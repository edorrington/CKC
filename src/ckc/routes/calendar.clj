(ns ckc.routes.calendar
  (:require [compojure.core :refer :all]
            [ckc.layout :as layout]
            [noir.session :as session]
            [noir.response :as resp]
            [ckc.db.core :as db]
            [ckc.util :as util]
            [clj-time.core :as ct]
            [clj-time.format :as f]
            [clj-time.coerce :as c]
            [clj-time.local :refer [format-local-time]]
            [clj-time.periodic :refer [periodic-seq]]
            [clojure.string :as s]
            [clj-time.local :refer [format-local-time]]
            [noir.util.route :refer [def-restricted-routes]]))

; Todo: deal with db exception here
(defn create-appointment [params]
  (let [appt (db/make-new-appointment<! params)]
    (if appt
      (resp/json {:id (:id appt)})
      (resp/json {}))))

(defn update-appointment [params]
  (let [site_id (:site_id (session/get :user))]
    (db/update-appointment! (assoc params :site_id site_id))
    (resp/json {:status "OK"})))

(defn update-appt-times [params]
  (db/update-appointment-times! (assoc params :site_id (:site_id (session/get :user))))
  (resp/json {:status "OK"}))

(defn create-event [params]
  (let [event (db/make-new-outreach-event<! params)]
    (if event
      (resp/json {:id (:id event)})
      (resp/json {}))))

(defn update-event [params]
  (let [site_id (:site_id (session/get :user))]
    (db/update-event! (assoc params :site_id site_id))
    (resp/json {:status "OK"})))

(defn update-event-times [params]
  (db/update-event-times! (assoc params :site_id (:site_id (session/get :user))))
  (resp/json {:status "OK"}))

(defn delete-event [id]
  (let [site_id (:site_id (session/get :user))]
    (db/delete-event! {:site_id site_id, :id id})
    (resp/json {:status "OK"})))

(defn delete-appointment [id]
  (let [site_id (:site_id (session/get :user))]
    (db/delete-appointment! {:site_id site_id, :id id})
    (resp/json {:status "OK"})))

(defn calendar-page []
  (layout/render-file "calendar.html"
      {:site_id (:site_id (session/get :user))
       :editable (not (some #{(:role (session/get :user))} ["admin" "user" "evaluator"]))
       :assisters (db/get-assisters)
       :outreach_event_types (db/get-outreach-event-types)}))

; ===================================================================================================================
; Schedules

;schedule_id, start_time, end_time, slots
(defn mk-none-sched [schedule hm]
 (let [t (c/from-sql-date (:repeat_begin schedule))
       item_start (ct/date-time (ct/year t) (ct/month t) (ct/day t) (:sh hm) (:sm hm))
       item_end (ct/date-time (ct/year t) (ct/month t) (ct/day t) (:eh hm) (:em hm))
       slots (:slots hm)]
   (db/make-new-schedule-item<! {:schedule_id (:id schedule)
                                 :start_time item_start
                                 :end_time item_end
                                 :slots slots})))

(defn expand-schedule [schedule start end slots]
  (let [repeater-fn (resolve (symbol (str "ckc.routes.api/mk-" (.toLowerCase (:repeats schedule)) "-sched")))
        [sh sm] (map #(Integer. %) (s/split start #":"))
        [eh em] (map #(Integer. %) (s/split end #":"))]
    (repeater-fn
      schedule
      {:sh sh
       :sm sm
       :eh eh
       :em em
       :slots slots})))

(defn schedule-page []
  (layout/render-file "schedule.html"
      {:site_id (:site_id (session/get :user))
       :editable (= (:role (session/get :user)) "site-admin")
       :assisters (db/get-assisters)}))

(defn create-schedule [params]
  (prn params)
  (if-let [schedule (db/make-new-schedule<! (assoc params :site_id (:site_id (session/get :user))))]
    (do
      (expand-schedule schedule (:start_time params) (:end_time params) (:slots params))
      (resp/json {:id (:id schedule)}))
    (resp/json {})))

(defn update-schedule [params]
  (db/update-schedule! (assoc params :site_id (:site_id (session/get :user))))
  (resp/json {:status "OK"}))

(defn update-schedule-times [params]
  (db/update-schedule-times! (assoc params :site_id (:site_id (session/get :user))))
  (resp/json {:status "OK"}))

; If kind == 'all' change the schedule with id to have an end-date of the passed date - 1 day
; If kind == 'one' split the schedule into two pieces, one with an end date of the passed date - 1 day
; and one with a start date of the passed date, and the end date of the original schedule.
; Finally, presumably the schedule is a non-repeat, so just delete it
(defn delete-schedule [id occ kind]
  (let [site_id (:site_id (session/get :user))]
    (cond
      (= kind "all")
      (db/delete-future-schedule! {:id id, :site_id site_id, :date occ})

      (= occ "one")
      (db/anatran
        (db/delete-future-schedule! {:id id, :site_id site_id, :date occ} {:connection tx})
        (db/split-schedule<! {:id id, :site_id site_id, :date occ} {:connection tx}))

      true
      (db/delete-schedule! {:id id, :site_id site_id})))
  (resp/json {:status "OK"}))





(def-restricted-routes calendar-routes
  (GET "/calendar" [] (calendar-page))
  (POST "/calendar/appointments" {params :params} (create-appointment params))
  (POST "/calendar/events" {params :params} (create-event params))
  (PUT "/calendar/appointments/:id" {params :params} (update-appointment params))
  (PUT "/calendar/events/:id" {params :params} (update-event params))
  (PUT "/calendar/events/:id/when" {params :params} (update-event-times params))
  (PUT "/calendar/appointments/:id/when" {params :params} (update-appt-times params))
  (DELETE "/calendar/appointments/:id" [id] (delete-appointment id))
  (DELETE "/calendar/events/:id" [id] (delete-event id))
  (GET "/calendar/schedule" [] (schedule-page))
  (POST "/calendar/schedule" {params :params} (create-schedule params))
  (PUT "/calendar/schedule/:id" {params :params} (update-schedule params))
  (PUT "/calendar/schedule/:id/when" {params :params} (update-schedule-times params))
  (DELETE "/calendar/schedule/:id/:occ/:kind" [id occ kind] (delete-schedule id (c/to-sql-date (c/from-long (Long. occ))) kind)))

