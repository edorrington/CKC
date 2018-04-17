(ns ckc.db.core
  (:import com.mchange.v2.c3p0.ComboPooledDataSource)
  (:require [ckc.db.schema :as schema]
            [clojure.java.jdbc :as jdbc]
            [clojure.java.io :as io]
            [noir.session :as session]
            [yesql.core :refer [defqueries]]))


(defn list-matching-resources [regex]
  (let [jar (java.util.jar.JarFile. (-> :keyword class (.. getProtectionDomain getCodeSource getLocation getPath)))
        entries (.entries jar)]
    (loop [result  []]
      (if (.hasMoreElements entries)
        (let [entry (.. entries nextElement getName)
              matches (re-find regex entry)]
          (if matches
            (recur (conj result entry))
            (recur result)))
        result))))

(defn pool
  [spec]
  (let [props (java.util.Properties.)
        cpds (doto (ComboPooledDataSource.)
               (.setDriverClass (:classname spec))
               (.setJdbcUrl (str "jdbc:" (:subprotocol spec) ":" (:subname spec)))
               (.setUser (:user spec))
               (.setPassword (:password spec))
               ;; expire excess connections after 30 minutes of inactivity:
               (.setMaxIdleTimeExcessConnections (* 30 60))
               ;; expire connections after 3 hours of inactivity:
               (.setMaxIdleTime (* 3 60 60)))]
    (.setProperty props "stringtype" "unspecified")
    (.setProperty props "password" (:password spec))
    (.setProperties cpds props)
    {:datasource cpds}))

(defonce +db-pool+ (pool schema/db-spec))

; First redefine execute-handler
(in-ns 'yesql.generate)
(require '[noir.session :as session])
(require '[cheshire.core :as json])
(defn execute-handler [db sql-and-params call-options]
  (let [user (session/get :user)
        user_id (:id user)
        site_id (:site_id user)
        what (json/encode sql-and-params)
        res (first (jdbc/execute! db sql-and-params))]
    (jdbc/db-do-prepared db "insert into audit_records (user_id, site_id, what) values (?, ?, ?)" (list user_id site_id what))
    res))

(defn insert-handler [db [statement & params] call-options]
  (let [user (session/get :user)
        user_id (:id user)
        site_id (:site_id user)
        what (json/encode (conj params statement))
        res  (jdbc/db-do-prepared-return-keys db statement params)]
    (jdbc/db-do-prepared db "insert into audit_records (user_id, site_id, what) values (?, ?, ?)" (list user_id site_id what))
    res))
(in-ns 'ckc.db.core)

; Run defqueries on all the .sql files in the resources/sql directory (or within the jar file)
(let [sep java.io.File/separator
      cs (-> :keyword class (.. getProtectionDomain getCodeSource getLocation getPath))
      isJar? (>= (.indexOf (last (clojure.string/split cs (re-pattern sep))) "ckc") 0)
      prefix (str "sql" sep)]
  (if isJar?
    (doseq [file (list-matching-resources #"^sql/.+\.sql$")]
      (defqueries file {:connection +db-pool+}))
    (doseq [file (filter #(re-find #".+\.sql$" %) (.list (io/file (io/resource "sql"))))]
      (defqueries (str prefix file) {:connection +db-pool+}))))

(defmacro anatran
  "Evaluates body in the context of a transaction on the pooled database connection.
  The macro anaphorically creates a binding called tx which is used within the body
  to specify the connection to use within the queries."
  [& body]
  (let [tx 'tx]
    `(jdbc/db-transaction* +db-pool+
        (^{:once true} fn* [~tx] ~@body))))

(defmacro q [& body]
  `(jdbc/query +db-pool+ ~@body))

(defn get-user [id]
  (let [user (first (get-user-by-id {:id id}))
        site_name (first (get-site-name {:id (:site_id user)}))]
    (if user (assoc user :site_name site_name) nil)))

(defn usersforsite [site]
  (if (string? site)
    (get-users-for-site-name {:name site})
    (if (zero? site) (get-users) (get-users-for-site {:id site}))))

(defn sitesmap []
  (reduce (fn [h s] (assoc h (:id s) (:name s))) {} (get-sites)))

(defn get-site [id]
  (first (get-site-by-id {:id id})))

(defn get-sites-with-admins []
  (map (fn [site]
         (assoc site :admins
           (get-site-admins-for-site {:id (:id site)}
                                     {:row-fn :id})))
       (get-sites)))

(defn members-for [fid type]
  (get-members-for-family {:family_id fid :type type} {:row-fn :name}))

(defn get-services-for-case [cid]
  (let [guardians (members-of-type-for-case {:case_id cid :member_type "guardian"})
        children (members-of-type-for-case {:case_id cid :member_type "child"})
        other (members-of-type-for-case {:case_id cid :member_type "other"})]
    {:guardians (map (fn [mrec] (assoc mrec :svcs (services-for-case-and-member {:case_id cid :member_id (:member_id mrec)}))) guardians)
     :children (map (fn [mrec] (assoc mrec :svcs (services-for-case-and-member {:case_id cid :member_id (:member_id mrec)}))) children)
     :others (map (fn [mrec] (assoc mrec :svcs (services-for-case-and-member {:case_id cid :member_id (:member_id mrec)}))) other)}))

(defn get-history-for-family [family_id site_id]
  (map (fn [cc]
         {:case_id (:case_id cc)
          :contacted_at (:contacted_at cc)
          :services (services-for-contact {:case_contacts_id (:id cc)})
          :notes (:notes cc)
          :user (get-user (:user_id cc))})
       (get-contacts-for-family {:family_id family_id, :site_id site_id})))

(defn get-history-for-case [case_id site_id]
  (let [case_contacts (get-contacts-for-case {:case_id case_id})]
    (map (fn [cc]
           (let [svcs (services-for-contact {:case_contacts_id (:id cc)})]
             {:contacted_at (:contacted_at cc)
              :services (clojure.string/join "; "
                                             (map #(str (:name %) " -> " (:service_provided %) " (" (:coverage_type %) ")")
                                                  svcs))
              :notes (:notes cc)
              :user (get-user (:user_id cc))})) case_contacts)))

(defn get-school-types []
  (reduce (fn [h st] (assoc h (:id st) (:name st))) {} (school-types)))

(defn get-schools-for-site [site_id]
  (reduce (fn [h s]
            (assoc h (:school_type_id s)
              (conj (get h (:school_type_id s) [])
                    (:name s))))
          {}
          (schools-for-site {:site_id site_id})))

(defn contact-types []
  (reduce (fn [h i] (let [ctc (:contact_type_category_id i)
                          v (get h ctc [])]
                      (assoc h ctc (conj v (dissoc i :contact_type_category_id)))))
          {}
          (get-contact-types)))

(defn duration
  "Returns the duration (in minutes) of an appointment"
  [appt]
  (/ (- (.getTime (:end_time appt)) (.getTime (:start_time appt))) 60000))

(defn audit
  ([what] (audit what "miscellaneous"))
  ([what kind]
    (if (bound? (var session/*noir-session*))
      (let [u (session/get :user)
            s (:site_id u)]
        (jdbc/db-do-prepared +db-pool+ "insert into audit_records (user_id, site_id, kind, what) values (?, ?, ?, ?)" (list (:id u) s kind what)))
      (jdbc/db-do-prepared +db-pool+ "insert into audit_records (user_id, site_id, kind, what) values ('ed', 0, ?, ?)" (list kind what)))))

;for each 1..n
; Pick a random case #
;   Pick 1..(# members) members
;     Pick a random service & coverage type
;       create a case_services record
;     end
;   end
; end
;end
(defn gensvcs []
  (let [now (.getTime (java.util.Date.))
        cases (q "select id from cases" :row-fn :id)]
    (doseq [c cases]
      (dotimes [_ (rand-int 3)]
        (let [sd (java.util.Date. (- now (* 86400000 (rand-int 100))))
              allmems (q ["select m.id from members m, cases c where c.id = ? and c.family_id = m.family_id" c] :row-fn :id)
              mems (take (rand-int (count allmems)) (shuffle allmems))
              contact (new-contact<! {:case_id c, :user_id "john", :contact_type_id (rand-nth [30 31 32]), :notes ""})]
          (doseq [m mems]
            (let [svc (rand-nth (q "select name from services_provided_types" :row-fn :name))
                  cvg (rand-nth (q "select name from coverage_types" :row-fn :name))]
              (create-case-service<! {:case_contacts_id (:id contact), :member_id m, :service_provided svc, :coverage_type cvg, :notes ""}))))))))

(defn get-service-types [section_id]
  (get-services-provided-types {:section_id section_id} {:row-fn :name}))
