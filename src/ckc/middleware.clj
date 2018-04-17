(ns ckc.middleware
  (:require [taoensso.timbre :as timbre]
            [selmer.parser :as parser]
            [environ.core :refer [env]]
            [selmer.middleware :refer [wrap-error-page]]
            [ring.middleware.anti-forgery :refer [wrap-anti-forgery]]
            [ring.middleware.flash :refer [wrap-flash]]
            [prone.middleware :refer [wrap-exceptions]]
            [noir-exception.core :refer [wrap-internal-error]]))

(defn log-request [handler]
  (fn [req]
    (timbre/debug req)
    (handler req)))

(def development-middleware
  [wrap-error-page
   wrap-flash
   wrap-anti-forgery
   wrap-exceptions])

(def production-middleware
  [#(wrap-internal-error % :log (fn [e] (timbre/error e))) wrap-flash wrap-anti-forgery])

(defn load-middleware []
  (concat (when (env :dev) development-middleware)
          production-middleware))

