(ns ckc.validators
  (:require [noir.validation :as vali]
            [noir.util.crypt :as crypt]
            [clj-time.core :as ct]
            [clj-time.format :as f]
            [ckc.db.core :as db])
  (:use [taoensso.timbre :only [trace debug info warn error fatal]]))

(defn valid-pass? [user oldpass pass1 pass2]
  (vali/rule (vali/has-value? user)
             [:uid "You must be logged in!"])
  (vali/rule (vali/has-value? oldpass)
             [:cpass "Please enter your current password"])
  (vali/rule (crypt/compare oldpass (:pass user))
             [:cpass "Incorrect password"])
  (vali/rule (vali/min-length? pass1 5)
             [:npass "New password must be at least 5 characters"])
  (vali/rule (= pass1 pass2)
             [:npass "New passwords do not match"])
  (not (vali/errors?)))

; Todo: Fill this in
(defn validate-family? [family members emails phones]
  ;(vali/rule false [:blerg "Don't do that!"])
  (not (vali/errors?)))

; Todo: Fill this in
(defn validate-contact? [params]
  (vali/rule false [:blerg "Nope!"])
  (not (vali/errors?)))

(defn valid-dates? [params]
  (let [fmt (f/formatter "dd/mm/yyyy")
        sd (:startdate params)
        ed (:enddate params)
        start (try (f/parse fmt sd) (catch Exception _ nil))
        end (try (f/parse fmt ed) (catch Exception _ nil))]
    (vali/rule (vali/has-value? sd)
               [:startdate "Start date must not be blank"])
    (vali/rule (vali/has-value? ed)
               [:enddate "End date must not be blank"])
    (if (and start end) (vali/rule (ct/after? end start)
               [:dates "End date must be after start date"])))
  (not (vali/errors?)))

(defn errors?
  "Just delegates to noir.validation/get-errors"
  [& [field]]
  (vali/errors? field))


(defn get-errors
  "Just delegates to noir.validation/get-errors"
  [& [field]]
  (vali/get-errors field))
