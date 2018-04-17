(ns ckc.routes.contacts
  (:require [compojure.core :refer :all]
            [cheshire.core :as cc]
            [ckc.layout :as layout]
            [noir.session :as session]
            [noir.util.route :refer [def-restricted-routes restricted]]
            [noir.response :as resp]
            [noir.request :as req]
            [ckc.db.core :as db]
            [ckc.validators :as v]
            [ckc.util :as util]
            [clj-time.core :as t]
            [clj-time.format :as f]
            [clojure.string :as s])
  (:use [taoensso.timbre :only [trace debug info warn error fatal]]
        [noir.request :only [*request*]]))

(defn- appt-rec [appt]
  (let [fmt (f/formatters :date-time)
        datevals (map #(Integer/parseInt %) (s/split (:date appt) #"/"))
        startvals (map #(Integer/parseInt %) (s/split (:time appt) #":"))
        durvals (map #(Integer/parseInt %) (s/split (:duration appt) #":"))
        start (t/local-date-time (nth datevals 2) (nth datevals 0) (nth datevals 1) (nth startvals 0) (nth startvals 1))
        end (t/plus start (t/hours (nth durvals 0)) (t/minutes (nth durvals 1)))]
    {:start_time (f/unparse-local fmt start), :end_time (f/unparse-local fmt end)}))

(defn new-contact-page [family_id]
  (let [user_id (:id (session/get :user))
        site_id (:site_id (session/get :user))
        case (first (db/current-case-for {:family_id family_id, :site_id site_id}))
        members (db/get-all-members-for-family {:family_id family_id})
        history (db/get-history-for-case (:id case) site_id)]
    (layout/render-file "contacts/new-contact.html"
      {:user_id user_id
       :site_id site_id
       :case case
       :members members
       :history history
       :family_services (db/get-service-types 2)
       :member_services (db/get-service-types 1)
       :coverages (db/get-coverage-types)
       :consent_signed (:consent_signed (first (db/get-family {:id family_id})))
       :referer (or ((:headers *request*) "referer") "/")
       :assisters (db/get-assisters)
       :case_statuses (db/case-statuses)
       :contact_type_categories (db/get-contact-type-categories)
       :contact_types (cc/encode (db/contact-types))
       :errors (session/flash-get :errors)})))

(defn create-contact [params]
  (let [site_id (:site_id (session/get :user))
        user_id (:id (session/get :user))
        case_id (:cid params)
        status (:status params)
        referred_from (:referred_from params)
        notes (:notes params)
        ctype (:contact_type params)
        appt (:appointment params)
        redir (or (:redir params) "/")
        members (:members params)
        title (clojure.string/join " & " (db/guardians-for-case {:case_id case_id} {:row-fn :name}))
        signed (not (not (:consent_signed params)))]
    (if (v/validate-contact? params)
      (do
        (db/anatran
          ; First update the families consent_signed field
          (db/update-case-consent! {:id case_id, :signed signed} {:connection tx})

          (let [cc (db/new-contact<! {:case_id case_id, :user_id user_id, :notes notes, :contact_type_id ctype} {:connection tx})]
            (db/update-case-status-and-ref! {:id case_id, :status status, :referred_from referred_from} {:connection tx})
; TODO: LOOK AT THIS PART
; (also, figure out what to do with "referrals_provided" param for "family"-level svcs)
            (doseq [member_id (keys members)]
              (let [member (get members member_id)
                    svc (:service_provided member)
                    cvg (:coverage_type member)
                    notes (:notes member)]
                (if (and svc cvg)
                  (if (and (string? svc) (string? cvg))
                    (db/create-case-service<! (merge member {:case_contacts_id (:id cc) :member_id member_id}) {:connection tx})
                    (dotimes [idx (count svc)]
                      (db/create-case-service<! {:case_contacts_id (:id cc)
                                                 :member_id member_id
                                                 :service_provided (nth idx svc)
                                                 :coverage_type (nth idx cvg)
                                                 :notes (nth idx notes)} {:connection tx})))))))
; END TODO
          (if (not (empty? (:date appt)))
            (db/make-new-appointment<! (merge {:case_id case_id,
                                               :outreach_event_id nil,
                                               :type (:type appt),
                                               :title title,
                                               :location "",
                                               :description "",
                                               :r_email (:r_email appt),
                                               :r_mail (:r_mail appt),
                                               :r_sms (:r_sms appt)}
                                              (appt-rec appt))
                                       {:connection tx})))
        (session/flash-put! :msg "New contact created")
        (resp/redirect redir))
      (do
        (session/flash-put! :errors (v/get-errors))
        (resp/redirect (str "/contacts/" (:family_id (first (db/get-case {:id case_id}))) "/new"))))))

(defn quick-contact [params]
  (println params)
  (let [uid (:id (session/get :user))
        cid (:cid params)
        redir (or (:redir params) "/")
        appt (:appointment params)
        ctype (:contact_type params)
        status (:status params)
        notes (:notes params)
        signed (:consent_signed params)]
    (if (v/validate-contact? params)
      (do
        (db/anatran
          ; First, update the case
          (db/update-case-status! {:id cid, :status status} {:connection tx})
          ; Next, update the consent form signed flag
          (db/update-case-consent! {:id cid, :signed signed} {:connection tx})
          ; Now create a contact record
          (db/new-contact<! {:case_id cid,
                             :user_id uid,
                             :contact_type_id ctype,
                             :notes notes}
                            {:connection tx})
          ; Finally, if an appointment was scheduled, create the appointment record
          (if (not (empty? (:date appt)))
            (db/make-new-appointment<! (merge {:case_id cid,
                                               :outreach_event_id nil,
                                               :type (:type appt),
                                               :title (:title appt),
                                               :location "",
                                               :description "",
                                               :r_email (:r_email appt),
                                               :r_mail (:r_mail appt),
                                               :r_sms (:r_sms appt)}
                                              (appt-rec appt))
                                       {:connection tx})))
        (session/flash-put! :msg "Quick Contact created"))
      (session/flash-put! :errors (v/get-errors)))
    (resp/redirect redir)))

(def-restricted-routes contact-routes
  (GET "/contacts/:fid/new" [fid] (new-contact-page fid))
  (PUT "/contacts/:cid/new" {params :params} (create-contact params))
  (POST "/contacts/:cid/quick" {params :params} (quick-contact params)))
