(ns ckc.routes.auth
  (:use compojure.core)
  (:require [ckc.layout :as layout]
            [noir.session :as session]
            [noir.response :as resp]
            [ckc.validators :as v]
            [noir.util.crypt :as crypt]
            [noir.util.route :refer [restricted]]
            [ckc.db.core :as db])
  (:use [taoensso.timbre :only [trace debug info warn error fatal]])
  (:import javax.xml.bind.DatatypeConverter))

(defn profile [& [params]]
  (layout/render-file "profile.html"
                      (merge params
                             {:msg (session/flash-get :msg)
                              :errors (session/flash-get :errors)})))

(defn update-profile [{:keys [first-name last-name email oldpass pass1 pass2]}]
  (let [user (db/get-user (:id (session/get :user)))]
    (db/update-user! {:id (:id user) :first_name first-name :last_name last-name :email email})
    (if (and (seq pass1) (seq pass2)) ; Did user enter a new password?
      (if (v/valid-pass? user oldpass pass1 pass2)
        (do
          (db/update-user-password! {:id (:id user) :pass (crypt/encrypt pass1)})
          (session/flash-put! :msg "Your profile has been updated"))
        (session/flash-put! :errors (v/get-errors))))
    (resp/redirect "/profile")))

(defn login []
  (layout/render-file "login.html"))

(defn parse-creds [auth]
  (when-let [basic-creds (second (re-matches #"\QBasic\E\s+(.*)" auth))]
    (->> (String. (DatatypeConverter/parseBase64Binary basic-creds) "UTF-8")
         (re-matches #"(.*):(.*)")
         rest)))

(defn handle-login [auth]
  (let [site-tags {1 "Oakland Unified School District Central Family Resource Center"
                   2 "Hayward Unified School District HUB"
                   3 "San Leandro Unified School District Connecting Kids to Coverage"}
        [user pass] (parse-creds auth)
         account (db/get-user user)]
    (if (and account (crypt/compare pass (:pass account)))
      (do
        (session/put! :user (select-keys account [:id :first_name :last_name :email :role :site_id]))
        (session/put! :site-tag (get site-tags (:site_id account) ""))
        (resp/empty))
      (resp/status 401 (resp/empty)))))

(defn logout []
  (session/clear!)
  (resp/redirect "/"))

(defroutes auth-routes
  (GET "/profile" [] (restricted (profile)))
  (POST "/update-profile" {params :params} (restricted (update-profile params)))
  (GET "/login" [] (login))
  (POST "/login" req
        (handle-login (get-in req [:headers "authorization"])))
  (GET "/logout" [] (restricted (logout))))
