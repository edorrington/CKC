(ns ckc.routes.admin
  (:require [compojure.core :refer :all]
            [ckc.layout :as layout]
            [noir.session :as session]
            [noir.util.route :refer [def-restricted-routes]]
            [noir.response :as resp]
            [ckc.validators :as v]
            [noir.util.crypt :as crypt]
            [ckc.db.core :as db]
            [ckc.util :as util]
            [hiccup.core :refer [html]])
  (:use [taoensso.timbre :only [trace debug info warn error fatal]]))

;; Users
(defn users-page []
  (layout/render-file
    "admin/users.html"
    {:users (let [sm (db/sitesmap)]
              (map #(assoc % :site_name (sm (:site_id %)))
                   (db/usersforsite (:site_id (session/get :user)))))
     :msg (session/flash-get :msg)}))

(defn edit-user-page [& [params]]
  (let [uid (:id params)]
    (layout/render-file
      "admin/edit_user.html"
      (merge params {:edit_user (db/get-user uid)
                     :errors (session/flash-get :errors)}))))

(defn update-user-page [{:keys [id first-name last-name email oldpass pass1 pass2]}]
  (let [user (db/get-user id)]
    (db/update-user! {:id id :first_name first-name :last_name last-name :email email})
    (if (and (seq pass1) (seq pass2)) ; Did user enter a new password?
      (if (v/valid-pass? user oldpass pass1 pass2)
        (do
          (db/update-user-password! {:pass (crypt/encrypt pass1)})
          (session/flash-put! :msg (str id "'s profile has been updated"))
          (resp/redirect "/admin/users"))
        (do
          (session/flash-put! :errors (v/get-errors))
          (resp/redirect (str "/admin/users/" id)))))))

;; Sites
(defn sites-page []
  (layout/render-file
    "admin/sites.html"
    {:sites (db/get-sites-with-admins)
     :msg (session/flash-get :msg)}))

(defn edit-site-page [sid]
  (info (str "Editing site " sid))
  (info (db/get-site sid))
  (layout/render-file
    "admin/edit_site.html"
    {:edit_site (db/get-site sid)
     :site_users (db/get-users-for-site {:id sid})
     :errors (session/flash-get :errors)}))

; Todo: Implement this!
(defn update-site-page [params]
  (session/flash-put! :errors ["Update not implemented yet"])
  (resp/redirect (str "/admin/sites/" (:id params))))

(defn templates-page []
  (let [site_id (:site_id (session/get :user))]
    (layout/render-file "admin/templates.html"
      {:email {:schedule (first (db/get-reminder-schedule {:site_id site_id :type "email"}))
             :templates (db/get-templates-for-type {:type "email" :site_id site_id})}
       :mail {:schedule (first (db/get-reminder-schedule {:site_id site_id :type "mail"}))
             :templates (db/get-templates-for-type {:type "mail" :site_id site_id})}
       :sms {:schedule (first (db/get-reminder-schedule {:site_id site_id :type "sms"}))
             :templates (db/get-templates-for-type {:type "sms" :site_id site_id})}
       :languages (db/get-template-languages)
       :msg (session/flash-get :msg)
       :errors (session/flash-get :errors)
       })))

(defn- cud
  "Handles create update and delete of templates from form data"
  [params cfn ufn dfn type site_id tx]
  (doseq [key (keys params)]
    (case key
      :schedule identity
      :del (doseq [id (flatten (vector (:del params)))]
             (dfn {:id id} {:connection tx}))
      :new (let [data (:new params)
                 keyset (keys data)
                 multi? (sequential? (first (vals data)))]
             (if multi?
               (dotimes [i (count (first (vals data)))]
                 (when-not (= "" (nth (:content data) i))
                   (cfn
                     (assoc (zipmap keyset (map #(nth (get data %) i) keyset)) :type type :site_id site_id)) {:connection tx}))
               (when-not (= "" (:content data)) (cfn (assoc data :type type :site_id site_id) {:connection tx}))))
      (if (= "" (get-in params [key :content]))
        ; If the content is blank, delete the reminder rather than updating it.
        (dfn {:id key} {:connection tx})
        (ufn (assoc (get params key) :id key :type type) {:connection tx})))))

(defn update-templates [params]
  (let [site_id (:site_id (session/get :user))]
    (try
      (db/anatran
        (cud (:email params) db/new-reminder-template<! db/update-reminder-template! db/delete-reminder-template! "email" site_id tx)
        (cud (:mail params) db/new-reminder-template<! db/update-reminder-template! db/delete-reminder-template! "mail" site_id tx)
        (cud (:sms params) db/new-reminder-template<! db/update-reminder-template! db/delete-reminder-template! "sms" site_id tx)
        (db/update-reminder-schedule! (assoc (get-in params [:email :schedule]) :site_id site_id :type "email"))
        (db/update-reminder-schedule! (assoc (get-in params [:mail :schedule]) :site_id site_id :type "mail"))
        (db/update-reminder-schedule! (assoc (get-in params [:sms :schedule]) :site_id site_id :type "sms")))
    (catch org.postgresql.util.PSQLException err
      (session/flash-put! :errors (.getMessage err))
      (resp/redirect "/admin/templates")))
    (session/flash-put! :msg "Reminder template changes were saved")
    (resp/redirect "/admin/templates")))

(defn audit-page []
  (layout/render-file "admin/audit.html" {:records (db/get-all-audit-records)}))

(defn get-audit-data [params]
  (let [page (Integer/parseInt (:page params))
        size (Integer/parseInt (:size params))
        total (:count (first (db/audit-records-count)))
        order "executed_at"
        data (db/get-paged-audit-records {:limit size, :offset (* page size), :order order})]
    (resp/json {:total total, :headers ["id","name","user_id","site_id","executed_at","what"], :rows data})))

(def-restricted-routes admin-routes
  (GET "/admin/users" [] (users-page))
  (GET "/admin/users/:id" [id] (edit-user-page {:id id}))
  (POST "/admin/users/:id" {params :params} (update-user-page params))
  (GET "/admin/sites" [] (sites-page))
  (GET ["/admin/sites/:id", :id #"[0-9]+"] [id] (edit-site-page (read-string id)))
  (POST "/admin/sites/:id" {params :params} (update-site-page params))
  (GET "/admin/templates" [] (templates-page))
  (POST "/admin/templates" {params :params} (update-templates params))
  (GET "/admin/audit" [] (audit-page))
  (GET "/admin/audit/:size/:page.json" {params :params} (get-audit-data params)))
