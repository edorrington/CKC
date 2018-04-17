(ns ckc.routes.home
  (:require [compojure.core :refer :all]
            [selmer.parser :as p]
            [cheshire.core :as cc]
            [ckc.layout :as layout]
            [ckc.util :as util]
            [noir.util.route :refer [restricted]]
            [noir.session :as session]
            [noir.response :as resp]
            [noir.util.cache :as cache]
            [ckc.db.core :as db]
            [hiccup.core :refer [html]]))

(cache/set-timeout! 3600)

(defn home-page []
  (let [user (session/get :user)]
    (if user
      (case (:role user)
        "health-clerk"
        (let [site_id (:site_id user)
              families (db/get-families {:site_id site_id})
              with-members (map (fn [f] (assoc f
                                          :guardians (db/members-for (:id f) "guardian")
                                          :kids (db/members-for (:id f) "child")
                                          :sortname (first (db/sort-for {:family_id (:id f)} {:row-fn :sortname}))))
                                  families)]
          (layout/render-file "dashboards/health-clerk.html"
                              {:families with-members
                               :case_statuses (db/case-statuses)
                               :assisters (db/get-assisters)
                               :contact_type_categories (db/get-contact-type-categories)
                               :contact_types (cc/encode (db/contact-types))
                               :msg (session/flash-get :msg)
                               :errors (session/flash-get :errors)}))
        (layout/render-file (str "dashboards/" (:role user) ".html")))
      (layout/render-file "home.html"))))

(defn help-page []
  (cache/cache! :help (layout/render (util/md->html "/md/help.md") {:help-selected "active"})))

(defn genblerg [n]
  (resp/set-headers {"Content-Type" "application/msword",
                     "Content-Disposition" "attachment; filename=\"pages.docx\""}
        (p/render
          "<!DOCTYPE html><html><head></head><body>{% for p in pages %}<h1>This is page {{p}}</h1><p>Hi Mom!</p><br clear=all style='mso-special-character:line-break;page-break-before:always'>{% endfor %}</body></html>"
          {:pages (range (Integer/parseInt n))})))

(defn cptest []
  (let [f (fn [x] (seq (.getArray x)))
        g (fn [row] (-> row (update-in [:guardians] f) (update-in [:kids] f)))]
      (layout/render-file "families/families.html"
        {:families (db/anatran (db/get-all-family-data {} {:row-fn g}))
         :case_statuses (db/case-statuses)
         :assisters (db/get-assisters)
         :contact_type_categories (db/get-contact-type-categories)
         :contact_types (cc/encode (db/contact-types))
         :msg (session/flash-get :msg)
         :errors (session/flash-get :errors)})))


(defroutes home-routes
  (GET "/" [] (home-page))
  (GET "/blerg/:pages" [pages] (genblerg pages))
  (GET "/cptest" [] (cptest))
  (GET "/help" [] (help-page)))

