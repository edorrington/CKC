(ns ckc.routes.reports
  (:require [compojure.core :refer :all]
            [noir.response :as resp]
            [noir.session :as session]
            [clj-time.core :as ct]
            [clj-time.format :as f]
            [clojure.data.csv :as csv]
            [ckc.layout :as layout]
            [ckc.reminders.core :as rem]
            [ckc.db.core :as db]
            [ckc.validators :as v]
            [noir.util.route :refer [def-restricted-routes]]))

(defn reports-page []
  (layout/render-file "reports/reports.html"
    {:services (db/get-service-types 1)
     :coverage (db/get-coverage-types {} {:row-fn :name})
     :sites (db/get-sites)
     :errors (session/flash-get :errors)}))

(defn run-status-report [params]
  (if (not (v/valid-dates? params))
    (do
      (session/flash-put! :errors (v/get-errors))
      (resp/redirect "/reports"))
    (let [sd (:startdate params)
          ed (:enddate params)
          fname (str "ASR_" sd "_" ed ".csv")
          hdr ["Family ID" "Sort Name" "Name" "Date of Birth"]
          data (db/get-family-members {:from sd, :to ed})
          keys [:id :sortname :name :dob]
          v (vec (cons hdr (map (fn [row] (vec (map #(% row) keys))) data)))]
      (resp/content-type "text/csv"
                         (resp/set-headers {"Content-Disposition" (str "attachment; filename=\"" fname "\"" )}
                                           (with-out-str (csv/write-csv *out* v)))))))

(defn- expand-dates
  "Expands the date range sd-ed to a list of start/end dates
  where each pair is a full month (or partial month for the first
  and last items if sd is not the 1st of a month and if ed is not
  the last of a month). If sd and ed do not span more than 1 month
  (i.e. they're both within the same month), just return a list
  consisting of sd and ed."
  [sd ed]
  (let [fmt (f/formatter "MM/dd/yyyy")
        startdate (f/parse fmt sd)
        enddate (f/parse fmt ed)
        samemonth (and (= (ct/month startdate) (ct/month enddate)) (= (ct/year startdate) (ct/year enddate)))
        nextmonth (ct/first-day-of-the-month (ct/plus startdate (ct/months 1)))]
    (if samemonth [{:from sd, :to ed}]
      (loop [s startdate e nextmonth dl []]
        (if (ct/after? e enddate)
          (conj dl {:from (f/unparse fmt s), :to (f/unparse fmt enddate)})
          (recur e (ct/plus e (ct/months 1)) (conj dl {:from (f/unparse fmt s), :to (f/unparse fmt e)})))))))

(defn run-outreach-report [params]
  (if (v/valid-dates? params)
    (let [type (:type params)
          sd (:startdate params)
          ed (:enddate params)
          sid (:site_id params)
          datelist (if (= type "range")
                     [{:from sd, :to ed}]
                     (expand-dates sd ed))]
      (layout/render-file "reports/outreach.html"
          {:type type
           :data
            (map (fn [m]
                   (let [em (assoc m :site_id sid)]
                     {:out-calls (first (db/outreach-calls-made em {:row-fn :count}))
                      :in-calls (first (db/incoming-calls-fielded em {:row-fn :count}))
                      :appts (first (db/scheduled-appointments em {:row-fn :count}))
                      :interested (first (db/interested-families2 em {:row-fn :count}))
                      :total-families (first (db/total-families-served em {:row-fn :count}))
                      :total-contacts (first (db/total-contacts em {:row-fn :count}))
                      :outreach-event-data (db/outreach-event-data em)}))
                 datelist)}))
    (do
      (session/flash-put! :errors (v/get-errors))
      (resp/redirect "/reports"))))


(def-restricted-routes reports-routes
  (GET "/reports" [] (reports-page))
  (GET "/reports/adhoc/choose" [] (layout/render-file "reports/adhoc.html"))
  (POST "/reports/status/run" {params :params} (run-status-report params))
  (POST "/reports/outreach/run" {params :params} (run-outreach-report params)))
