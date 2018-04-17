(ns ckc.routes.families
  (:require [compojure.core :refer :all]
            [selmer.parser :as parser]
            [ckc.layout :as layout]
            [noir.session :as session]
            [noir.util.route :refer [def-restricted-routes restricted]]
            [noir.response :as resp]
            [ckc.db.core :as db]
            [clojure.java.jdbc :as jdbc]
            [ckc.validators :as v]
            [ckc.util :as util]
            [clojure.edn :as edn]
            [clojure.string :as s]
            [cheshire.core :as cc]
            [hiccup.core :refer [html]])
  (:use [taoensso.timbre :only [trace debug info warn error fatal]]
        [noir.request :only [*request*]]))

(defn school-list [site_id]
  (let [st (db/get-school-types)
        schools (db/get-schools-for-site site_id)]
    (html (for [type (sort (keys schools))]
            [:optgroup {:label (st type)}
             (for [school (schools type)]
               [:option school])]))))

(parser/add-tag! :schools
   (fn [args cmap]
     (let [[curr] args
           cschool (get-in cmap (map keyword (s/split curr #"\.")))
           stypes (db/get-school-types)
           slist (db/get-schools-for-site (:site_id (session/get :user)))]
       (str
         (s/join (for [type (sort (keys slist))]
           (str "<optgroup label=\"" (stypes type) "\">"
                (s/join (for [school (slist type)]
                              (if (= school cschool)
                                (str "<option selected=\"selected\">" school "</option>")
                                (str "<option>" school "</option>"))))
                "</optgroup>")))))))

(defn families-page []
  (let [site_id (:site_id (session/get :user))
        f (fn [x] (seq (.getArray x)))
        g (fn [row] (-> row (update-in [:guardians] f) (update-in [:kids] f)))]
      (layout/render-file "families/families.html"
        {:families
          (db/anatran
            (if (and site_id (not (= site_id 0)))
              (db/get-family-data {:site_id site_id} {:row-fn g})
              (db/get-all-family-data {} {:row-fn g})))
         :case_statuses (db/case-statuses)
         :assisters (db/get-assisters)
         :contact_type_categories (db/get-contact-type-categories)
         :contact_types (cc/encode (db/contact-types))
         :msg (session/flash-get :msg)
         :errors (session/flash-get :errors)})))

(defn family-page [fid ptype]
  (let [site_id (:site_id (session/get :user))
        family (first (db/get-family-for-site {:id fid, :site_id site_id}))
        guardians (db/get-members-for-family {:family_id fid :type "guardian"})
        kids (db/get-members-for-family {:family_id fid :type "child"})
        other (db/get-members-for-family {:family_id fid :type "other"})
        emails (db/get-emails-for-family {:family_id fid})
        phones (db/get-telephones-for-family {:family_id fid})
        history (if (= ptype 'view) (db/get-history-for-family fid site_id) nil)]
    (if family
        (layout/render-file (str "families/" ptype "-family.html")
          {:family family
           :guardians guardians
           :children kids
           :other other
           :emails emails
           :telephones phones
           :history history
           :errors (session/flash-get :errors)
           ; Lookup values
           :schools (school-list site_id)
           :referer (or ((:headers *request*) "referer") "/")
           :email_address_types (db/get-email-address-types {} {:row-fn :name})
           :telephone_types (db/get-telephone-types {} {:row-fn :name})
           :ethnicities (db/get-ethnicities)
           :languages (db/get-languages)})
        (layout/render "<h3>No such family exists. Please try again.</h3>"))))

(defn- calc-sortname
  "If a member doesn't have an existing sortname,
  calculates an initial take based on their name"
  [name]
  (let [prelist ["mr" "mrs" "ms" "miss" "rev" "dr" "sir" "hon"]
        postlist ["jr" "sr" "ii" "iii" "iv" "ph.d" "md" "m.d"]
        names (clojure.string/split (or name "") #"[\t ,\?\.]+")
        lname (if (some #{(.toLowerCase (last names))} postlist) (nth names (- (count names) 2)) (last names))
        fname (if (some #{(.toLowerCase (first names))} prelist) (second names) (first names))]
    (str lname " " fname)))

(defn- fill-sortname
  "Attempts to fill in the sortname field for each member
  which has a blank sortname. Tries to parse the name field
  and if possible, inserts sortname as last_name, first_name."
  [members]
  (let [newmembers (assoc-in members [:new :sortname] (vec (map #(calc-sortname %) (get-in members [:new :name]))))
        oldkeys (keys (dissoc members :new))]
    (loop [m newmembers keys oldkeys]
      (if (empty? keys)
        m
        (let [k (first keys)]
          (recur
            (assoc-in m [k :sortname]
                      (if (empty? (get-in m [k :sortname]))
                        (calc-sortname (get-in m [k :name]))
                        (get-in m [k :sortname])))
            (rest keys)))))))

(defn- cud
  "Handles create update and delete from form data"
  [params cfn ufn dfn fid tx]
  (doseq [key (keys params)]
    (case key
      :del (doseq [id (flatten (vector (:del params)))]
             (dfn {:id id} {:connection tx}))
      :new (let [data (:new params)
                 keyset (keys data)
                 multi? (sequential? (first (vals data)))]
             (if multi?
               (dotimes [i (count (first (vals data)))]
                 (cfn
                   (assoc (zipmap keyset (map #(nth (get data %) i) keyset)) :family_id fid)) {:connection tx})
               (cfn (assoc data :family_id fid) {:connection tx})))
      (ufn (assoc (get params key) :id key) {:connection tx}))))

(defn update-family [params]
  (let [fid (:id params)
        family (assoc (:family params) :id fid :consent_signed (:consent_signed (:family params)))
        members (:member params)
        emails (:email_address params)
        phones (:telephone_number params)]
    (if (v/validate-family? family members emails phones)
      (try
        (db/anatran
          ; update core family data
          (db/update-family! family {:connection tx})
          ; update family member data
          (cud (fill-sortname members) db/new-member-for-family<! db/update-member! db/delete-member! fid tx)
          ; update email addresses
          (cud emails db/new-email-for-family<! db/update-email-address! db/delete-email-address! fid tx)
          ; update telephone numbers
          (cud phones db/new-phone-for-family<! db/update-telephone-number! db/delete-telephone-number! fid tx))
        (session/flash-put! :msg "Family record updated")
        (resp/redirect "/families")
        (catch org.postgresql.util.PSQLException err
          (session/flash-put! :errors (.getMessage err))
          (resp/redirect (str "/families/" (:id params) "/edit"))))
      (do
        (session/flash-put! :errors (v/get-errors))
        (resp/redirect (str "/families/" (:id params) "/edit"))))))

(defn create-family [params]
  ; Essentially the same as update-family (maybe can consolidate?)
  (let [site_id (:site_id (session/get :user))
        family (:family params)
        members (:member params)
        emails (:email_address params)
        phones (:telephone_number params)
        newContact? (= (:act params) "newcontact")
        redir (or (:redir params) "/")]
    (if (v/validate-family? family members emails phones)
      (try
        (db/anatran
          (let [family (db/create-family<! (assoc family :site_id site_id :consent_signed (:consent_signed family)) {:connection tx})
                fid (:id family)]
            (cud (fill-sortname members) db/new-member-for-family<! nil nil fid tx)
            ; update email addresses
            (cud emails db/new-email-for-family<! nil nil fid tx)
            ; update telephone numbers
            (cud phones db/new-phone-for-family<! nil nil fid tx)
            ; create case record for family
            (let [newcase (db/create-case<! {:family_id fid, :status "Initial"} {:connection tx})]
              (session/flash-put! :msg "New Family record created")
              (resp/redirect
                (if newContact?
                  (str "/contacts/" (:id newcase) "/new")
                  redir)))))
        (catch org.postgresql.util.PSQLException err
          (session/flash-put! :errors (.getMessage err))
          (resp/redirect (str "/families/new"))))
      (do
        (session/flash-put! :errors (v/get-errors))
        (resp/redirect "/families/new")))))

(defn new-family-page [headers]
    (layout/render-file "families/new-family.html"
      {:email_address_types (db/get-email-address-types {} {:row-fn :name})
       :telephone_types (db/get-telephone-types {} {:row-fn :name})
       :ethnicities (db/get-ethnicities)
       :referer (get headers "referer")
       :schools (school-list (:site_id (session/get :user)))
       :languages (db/get-languages)
       :errors (session/flash-get :errors)}))

(defn bycid [case_id]
  (let [site_id (:site_id (session/get :user))
        fid (first (db/get-family-for-case {:case_id case_id, :site_id site_id} {:row-fn :id}))]
    (resp/redirect (str "/families/" fid))))

(def-restricted-routes family-routes
  (GET "/families" [] (families-page))
  (GET ["/families/:id", :id #"[0-9]+"] [id] (family-page (edn/read-string id) 'view))
  (GET ["/families/bycid/:cid", :cid #"[0-9]+"] [cid] (bycid (edn/read-string cid)))
  (GET ["/families/:id/edit", :id #"[0-9]+"] [id] (family-page (edn/read-string id) 'edit))
  (POST "/families/:id" {params :params} (update-family params))
  (GET "/families/new" {headers :headers} (new-family-page headers))
  (PUT "/families/new" {params :params} (create-family params)))
