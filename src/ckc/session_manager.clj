(ns ckc.session-manager
  (:require [noir.session :as session]
            [cronj.core :refer [cronj]]))

(defn- clear-expired [_ _]
  (session/clear-expired-sessions))

(def cleanup-job
  (cronj
   :entries
   [{:id "session-cleanup"
     :handler clear-expired
     :schedule "* /30 * * * * *"
     :opts {}}]))
