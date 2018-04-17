(ns ckc.routes.referrals
  (:require [compojure.core :refer :all]
            [ckc.layout :as layout]
            [noir.session :as session]
            [noir.util.route :refer [def-restricted-routes]]
            [noir.response :as resp]
            [ckc.db.core :as db]
            [ckc.util :as util]
            [hiccup.core :refer [html]])
  (:use [taoensso.timbre :only [trace debug info warn error fatal]]))

(defn- xform [r needs svcs]
  (let [mog (reduce (fn [a i]
                      (let [{cnt :count svc :service_provided need :identified_need} i]
                        (assoc a need
                               (assoc (get a need {}) svc cnt))))
                    {}
                    r)]
    (map (fn [need]
           (let [cnts (get mog need)]
             {:name need
              :data (map (fn [svc]
                           (get cnts svc 0))
                         svcs)}))
         needs)))

(defn referrals-page []
  (let [site_id (:site_id (session/get :user))
        needs (db/get-service-types 2)
        svcs ["Information Referral", "Concrete Resource", "Warm Handoff to Agency"]
        year (xform (db/get-ytd-referrals {:site_id site_id}) needs svcs)
        month (xform (db/get-mtd-referrals {:site_id site_id}) needs svcs)]
  (layout/render-file "referrals.html"
    {:identified_needs  needs
     :services_provided svcs
     :ytd               year
     :thismonth         month
     :msg               (session/flash-get :msg)
     :errors            (session/flash-get :errors)})))

(defn add-referral [params]
  (let [site_id (:site_id (session/get :user))]
    (try
        (db/add-referral<! (assoc params :site_id site_id))
        (catch org.postgresql.util.PSQLException err
          (session/flash-put! :errors (.getMessage err))
          (resp/redirect "/referrals")))
    (session/flash-put! :msg "Referral added")
    (resp/redirect "/referrals")))

(def-restricted-routes referrals-routes
  (GET "/referrals" [] (referrals-page))
  (POST "/referrals" {params :params} (add-referral params)))
