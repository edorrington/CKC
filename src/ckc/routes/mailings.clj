(ns ckc.routes.mailings
  (:require [compojure.core :refer :all]
            [clojure.string :as s]
            [ckc.layout :as layout]
            [noir.util.route :refer [def-restricted-routes]]
            [noir.session :as session]
            [noir.response :as resp]
            [ckc.util :as util]
            [ckc.db.core :as db]
            [hiccup.core :refer [html]])
  (:use [taoensso.timbre :only [trace debug info warn error fatal]]))

(defn mailings-page []
  (layout/render-file "mailings/mailings.html"
    {:outstanding (db/outstanding-mails {:site_id (:site_id (session/get :user))})}))

(defn run-mailings [params]
  ; Get the appointments whose reminders we'll send
  ; Generate the reminder HTML document based on a template
  ; Convert the HTML to a docx file
  ; Return a JSON response showing success/failure along with
  ; the document name
  (let [filename (str "mailings_" (.format (java.text.SimpleDateFormat. "yyyyMMdd_HHmmss") (java.util.Date.)) ".docx")
        appts (db/get-appointments {:ids (:appts params)})]
    (resp/json {:status "success", :filename filename})))

(defn get-mailing [filename]
  (let [tf (clojure.java.io/file "/tmp" filename)]
    (if (.exists tf)
      {:status 200
       :headers {
                 "Content-Type" "application/msword"
                 "Content-Disposition" (str "attachment; filename=\"" filename "\"")}
       :body (java.io.FileInputStream. (.getAbsolutePath tf))}
      {:status 404
       :body (str "File '" filename "' doesn't exist. Please contact the system administrator.")})))

(def-restricted-routes mailing-routes
  (GET "/mailings" [] (mailings-page))
  (POST "/mailings/run" {params :params} (run-mailings params))
  (GET "/mailings/:filename" [filename] (get-mailing filename)))
