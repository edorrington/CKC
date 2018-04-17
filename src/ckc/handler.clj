(ns ckc.handler
  (:require [compojure.core :refer [defroutes]]
            [ckc.layout :as layout]
            [ckc.middleware :refer [load-middleware]]
            [ckc.session-manager :as session-manager]
            [noir.response :refer [redirect]]
            [noir.util.middleware :refer [app-handler]]
            [noir.util.route :refer [restricted]]
            [noir.session :as session]
            [compojure.route :as route]
            [taoensso.timbre :as timbre]
            [taoensso.timbre.appenders.rotor :as rotor]
            [selmer.parser :as parser]
            [environ.core :refer [env]]
            [ring.util.anti-forgery :refer [anti-forgery-field]]
            [ring.middleware.defaults :refer [site-defaults secure-site-defaults]]
            [ckc.routes.admin :refer [admin-routes]]
            [ckc.routes.api :refer [api-routes]]
            [ckc.routes.auth :refer [auth-routes]]
            [ckc.routes.calendar :refer [calendar-routes]]
            [ckc.routes.contacts :refer [contact-routes]]
            [ckc.routes.families :refer [family-routes]]
            [ckc.routes.home :refer [home-routes]]
            [ckc.routes.mailings :refer [mailing-routes]]
            [ckc.routes.referrals :refer [referrals-routes]]
            [ckc.routes.reports :refer [reports-routes]]
            [hiccup.core :refer [html]]
            [cronj.core :as cronj]))

(defn access-denied [msg]
  (fn [_] (layout/render (html
                           [:h1 "Access Denied"]
                           [:h3 msg]
                           [:a {:href "/"} "Please try again"]))))

(defn four-oh-four [req]
  (fn [_] (layout/render (html
                           [:h1 "Hmm, that page appears to be missing"]
                           [:a {:href "/"} "Please try again"]))))

(defn redir
  "Returns a function that will redirect to a given URL. This allows
  us to intercept those redirects for logging, etc."
  [to]
  (fn [req] (println "Redirecting to " to)(redirect to)))

(defroutes base-routes
  (route/resources "/")
  (route/not-found four-oh-four))

(defn init
  "init will be called once when
   app is deployed as a servlet on
   an app server such as Tomcat
   put any initialization code here"
  []
  (timbre/set-config!
    [:appenders :rotor]
    {:min-level :info
     :enabled? true
     :async? false ; should be always false for rotor
     :max-message-per-msecs nil
     :fn rotor/appender-fn})

  (timbre/set-config!
    [:shared-appender-config :rotor]
    {:path "ckc.log" :max-size (* 512 1024) :backlog 10})

  ;;set up CSRF protection
  (parser/add-tag! :csrf-token (fn [_ _] (anti-forgery-field)))

  (if (env :dev) (parser/cache-off!))
  ;;start the expired session cleanup job
  (cronj/start! session-manager/cleanup-job)
  (timbre/info "ckc started successfully"))

(defn destroy
  "destroy will be called when your application
   shuts down, put any clean up code here"
  []
  (timbre/info "ckc is shutting down...")
  (cronj/shutdown! session-manager/cleanup-job)
  (timbre/info "shutdown complete!"))

;; Define base access restrictions: all, user, site-admin, admin
(defn everyone [request] true)

(defn is-authed [request]
  (session/get :user))

(defn is-site-admin [request]
  (= "site-admin" (:role (session/get :user))))

(defn is-admin [request]
  (= "admin" (:role (session/get :user))))

(defn is-user [request]
  (= "user" (:role (session/get :user))))

(defn is-site-user [request]
  (= "site-user" (:role (session/get :user))))

(defn is-evaluator [request]
  (= "evaluator" (:role (session/get :user))))

(defn is-health-clerk [request]
  (= "health-clerk" (:role (session/get :user))))

(defn is-not-health-clerk [request] (not (is-health-clerk request)))

(def app (app-handler
           ;; add your application routes here
           [auth-routes api-routes mailing-routes family-routes contact-routes referrals-routes
            calendar-routes reports-routes admin-routes home-routes base-routes]
           ;; add custom middleware here
           :middleware (load-middleware)
           :ring-defaults (assoc-in (assoc-in site-defaults [:session :timeout] (* 30 60)) [:session :timeout-response] (redirect "/"))
           ;; add access rules here
           :access-rules [{:uri "/" :rule everyone}
                          {:uri "/referrals" :rule is-site-admin
                           :on-fail (access-denied "You must be a site administrator to access this page")}
                          {:uri "/admin/audit*" :rule is-admin
                           :on-fail (access-denied "You must be the global administrator to access this page")}
                          {:uri "/admin/sites" :rule is-admin
                           :on-fail (access-denied "You must be the global administrator to access this page")}
                          {:uri "/admin/*" :rules {:any [is-admin is-site-admin]}
                           :on-fail (access-denied "You must be an administrator to access this page")}
                          {:uri "/reports*" :rule is-not-health-clerk
                           :on-fail (access-denied "You do not have rights to access this page")}
                          {:uri "/*" :rule is-authed
                           :on-fail (redir "/")}]
           :formats [:json-kw :edn]))
