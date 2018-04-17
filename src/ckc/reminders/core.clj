(ns ckc.reminders.core
  (:require [ckc.db.core :as db]
            [noir.session :as session]
            [selmer.parser :as p]
            [clj-http.client :as client]
            [cheshire.core :as json]
            [twilio.core :as twilio]
            [environ.core :refer [env]]
            [clojure.string :as s]
            [ckc.util :as util]
            [hiccup.core :refer [html]]))

(defonce twilio-auth (env :twilio-auth))
(defonce twilio-sid (env :twilio-sid))
(defonce twilio-sendfrom (env :twilio-sendfrom))
(defonce mandrill-key (env :mandrill-key))

(defn make-url [route]
  (str "https://mandrillapp.com/api/1.0/" route ".json"))

; post a Mandrill API request
(defn post-request
  "Make a generic POST HTTP request"
  [route body]
  (try
    (let [url (make-url route)
          json (json/generate-string body)
          resp (client/post url
                {:accept :json
                 :content-type :json
                 :body json})
          output (json/parse-string (:body resp))]
      output)
  (catch Exception e
     (let [exception-info (.getData e)]
     (select-keys
       (into {} (map (fn [[k v]] [(keyword k) v])
         (json/parse-string
             (get-in exception-info [:object :body]))))
             (vector :status :message :code))))))

(defn send-email [to subject body]
  "Send transactional email through Mandrill"
  (post-request "messages/send" {:key mandrill-key
                                 :message {
                                           :subaccount "ckc"
                                           :html body
                                           :from_email "no-reply@ckc.dorrington.org"
                                           :subject subject
                                           :to (vec (map #(assoc {} :email %) to))
                                           }}))

(defn send-sms [to body]
  (twilio/with-auth twilio-sid twilio-auth (twilio/send-sms {:From twilio-sendfrom :To to :Body body})))

(defn render-appointment [appt type]
  (let [type (name type)
        family_id (:family_id appt)
        {template :content, language :language} (first (db/get-template {:type type, :family_id family_id}))]
    (util/render-template appt template language)))

(defn send-email-reminder
  "Sends the email reminder for a given appointment, updating its s_email flag"
  [aid]
  (let [appt (first (db/get-full-appointment {:id aid}))
        emails (db/get-emails-for-family {:family_id (:family_id appt)})]
    (send-email emails "Appointment Reminder" (render-appointment appt :email))))

(defn send-test-email-reminder
  "Sends a test email for a random appointment with a given template to a given address"
  [template language to]
  (let [site_id (:site_id (session/get :user))
        appt (first (db/get-full-appointment {:id (first (db/get-next-appointment-id {:site_id site_id} {:row-fn :id}))}))]
    (send-email [to] "Test Appointment Reminder" (util/render-template appt template language))))

(defn send-sms-reminder
  "Sends a text reminder for a given appointment, updating its s_sms flag"
  [aid]
  (let [appt (first (db/get-full-appointment {:id aid}))
        phone (first (db/get-telephones-for-family {:family_id (:family_id appt)} {:row-fn :telephone_number}))]
    (send-sms phone (render-appointment appt :sms))))

(defn send-test-sms-reminder
  "Sends a test email for a random appointment with a given template to a given address"
  [template language to]
  (let [site_id (:site_id (session/get :user))
        appt (first (db/get-full-appointment {:id (first (db/get-next-appointment-id {:site_id site_id} {:row-fn :id}))}))]
    (deref (send-sms to (util/render-template appt template language)))))

(defn send-test-mail-reminder [template language])

(defn generate-mailing [])

(defn send-test-reminder
  "Sends a test reminder of a given type"
  [type template language to]
  (case type
    "email" (send-test-email-reminder template language to)
    "mail" (send-test-mail-reminder template language)
    "sms" (send-test-sms-reminder template language to)))

