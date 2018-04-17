(ns ckc.db.schema (:require [environ.core :refer [env]]))


(def db-spec
  {:subprotocol "postgresql"
   :subname (env :database-url)
   :user (env :database-user)
   :password (env :database-password)})

