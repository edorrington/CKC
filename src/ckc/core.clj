(ns ckc.core
  (:require
    [ckc.handler :refer [app init destroy]]
    [ring.middleware.reload :as reload]
    [org.httpkit.server :as http-kit]
    [taoensso.timbre :as timbre])
  (:gen-class))

;contains function that can be used to stop http-kit server
(defonce server
  (atom nil))

(defn dev? [args] (let [isdev (some #{"-dev"} args)] (if isdev (println "Devmode") (println "Prodmode")) isdev))

(defn parse-port [args]
  (if-let [port (->> args (remove #{"-dev"}) first)]
    (Integer/parseInt port)
    3000))

(defn- start-server [port args]
  (init)
  (reset! server
          (http-kit/run-server
           (if (dev? args) (reload/wrap-reload app) app)
           {:port port :queue-size 102400 :thread (* 2 (.availableProcessors (Runtime/getRuntime)))})))

(defn- stop-server []
  (destroy)
  (@server))

(defn -main [& args]
  (let [port (parse-port args)]
    (start-server port args)
    (timbre/info "server started on port:" port)))
