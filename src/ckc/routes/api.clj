(ns ckc.routes.api
  (:require [compojure.core :refer :all]
            [noir.session :as session]
            [noir.util.route :refer [def-restricted-routes]]
            [noir.response :as resp]
            [clj-time.format :refer [parse]]
            [clj-time.coerce :refer [from-sql-date]]
            [clj-time.local :refer [format-local-time]]
            [clj-time.periodic :refer [periodic-seq]]
            [clojure.string :as s]
            [ckc.reminders.core :as rem]
            [ckc.db.core :as db])
  (:use [taoensso.timbre :only [trace debug info warn error fatal]]))

(defn- xform
  "Transform the data in the database into a form that fullCalendar
  can use most effectively."
  [vals]
  (map #(assoc (select-keys % [:id
                               :title
                               :location
                               :description
                               :type
                               :case_id
                               :site_id
                               :r_email
                               :r_mail
                               :r_sms
                               :enrollment_assistance
                               :attendance])
          :className (str "ev_" (:position %))
          :start (format-local-time (:start_time %) :date-time)
          :end (format-local-time (:end_time %) :date-time))
       vals))

(defn calendar-items
  "Returns a JSON list of appointments and events from <start> date to <end> date,
  only for the site associated with the logged-in user. If the user
  is a global user (User or Admin), then return all items within
  the given dates."
  [params]
  (let [site_id (:site_id (session/get :user))
        start (or (:start params) "1900-01-01")
        end (or (:end params) "2200-12-31")
        show (s/split (:show params) #"\|")
        appts (if (zero? site_id)
               (db/get-all-appointments {:start start, :end end, :show show})
               (db/get-appointments {:site_id site_id, :start start, :end end, :show show}))
        events (if (zero? site_id)
               (db/get-all-outreach-events {:start start, :end end})
               (db/get-outreach-events {:site_id site_id, :start start, :end end}))]
    (resp/json (xform (concat appts events)))))

(defn schedule-items
  "Returns a JSON list of appointment schedules from <start> date to <end> date,
  only for the site associated with the logged-in site-admin user."
  [params]
  (let [site_id (:site_id (session/get :user))
        show (s/split (:show params) #"\|")
        start (or (:start params) "1900-01-01")
        end (or (:end params) "2200-12-31")]
    (resp/json (db/get-schedules {:site_id site_id, :start start, :end end, :show show}
                                 {:row-fn #(assoc %
                                             :className (str "ev_" (:position %))
                                             :start (format-local-time (:start_time %) :date-time)
                                             :end (format-local-time (:end_time %) :date-time))}))))

(defn appointments
  "Returns a JSON list of appointments from <start> date to <end> date,
  only for the site associated with the logged-in user. If the user
  is a global user (User or Admin), then return all appointments within
  the given dates."
  [params]
  (let [site_id (:site_id (session/get :user))
        start (or (:start params) "1900-01-01")
        end (or (:end params) "2200-12-31")
        data (if (zero? site_id)
               (db/get-all-appointments {:start start, :end end})
               (db/get-appointments {:site_id site_id, :start start, :end end}))]
    (resp/json (xform data))))

(defn appointment
  "Returns a JSON record for an appointment with the given id"
  [id]
  (let [site_id (:site_id (session/get :user))]
    (resp/json (first (xform (db/get-appointment {:id id, :site_id site_id}))))))

(defn events
  "Returns a JSON list of outreach events from <start> date to <end> date,
  only for the site associated with the logged-in user. If the user
  is a global user (User or Admin), then return all events within
  the given dates."
  [params]
  (let [site_id (:site_id (session/get :user))
        start (or (:start params) "1900-01-01")
        end (or (:end params) "2200-12-31")
        data (if (zero? site_id)
               (db/get-all-outreach-events {:start start, :end end})
               (db/get-outreach-events {:site_id site_id, :start start, :end end}))]
    (resp/json (xform data))))

(defn event
  "Returns a JSON record for an outreach event with the given id"
  [id]
  (let [site_id (:site_id (session/get :user))]
    (resp/json (first (xform (db/get-outreach-event {:id id, :site_id site_id}))))))

(defn suggest_family
  "Given a query, look for families that might match"
  [query]
  (let [site_id (:site_id (session/get :user))
        families (db/matching-families {:site_id site_id, :query (str "%" query "%")})]
    (resp/json families)))

(defn test-reminder
  "POST-ed by the reminder templates page. Allows us to test sending a reminder
  to a given email address / phone number, or test a printed mailing."
  [params]
  (let [ttype (:type params)          ; email, mail, or sms
        template (:template params)   ; the template text, HTML for email or mail, text for sms
        language (:language params)   ; what language the template is in
        to (:to params)               ; "address" to send to for email or sms, null for mail
        result (rem/send-test-reminder ttype template language to)]
    (resp/json result)))

(def-restricted-routes api-routes
  (GET "/api/calendar_items.json" {params :params} (calendar-items params))
  (GET "/api/schedule_items.json" {params :params} (schedule-items params))
  (GET "/api/appointment/:id.json" [id] (appointment id))
  (GET "/api/events.json" {params :params} (events params))
  (GET "/api/event/:id.json" [id] (event id))
  (GET "/api/suggest_family/:q" [q] (suggest_family q))
  (POST "/api/reminder_test" {params :params} (test-reminder params)))
