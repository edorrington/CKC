(ns ckc.layout
  (:require [selmer.parser :as parser]
            [selmer.filters :as filters]
            [clojure.string :as s]
            [ring.util.response :refer [content-type response]]
            [compojure.response :refer [Renderable]]
            [environ.core :refer [env]]
            [ckc.util :as util]
            [noir.session :as session]))

(def template-path "templates/")

(parser/add-tag! :slcomment (fn [args context-map] (str "")))

(def timings (atom {}))
(parser/add-tag! :timing-start
                 (fn [args context-map]
                   (let [name (first args)
                         ts (System/nanoTime)]
                     (swap! timings #(assoc % name ts))
                     (str "<!-- Start timing (" name ") -->"))))

(parser/add-tag! :timing-end
                 (fn [args context-map]
                   (let [name (first args)
                         start (get @timings name)
                         end (System/nanoTime)]
                     (swap! timings #(dissoc % name))
                     (str "<!-- End timing (" name "): " (if start (str (/ (- end start) 1000000.0) " ms") "N/A") " -->"))))

(parser/add-tag! :opt (fn [args cmap content]
                        (let [[vstr cstr] args
                              value (get-in cmap (map keyword (s/split vstr #"\.")))
                              current (get-in cmap (map keyword (s/split cstr #"\.")))]
                          (str "<option value=\"" value "\"" (if (= value current) "selected=\"selected\">" ">") (get-in content [:opt :content]) "</option>")))
                 :endopt)
(parser/add-tag! :daysopt (fn [args cmap]
                        (let [[cstr] args
                              current (get-in cmap (map keyword (s/split cstr #"\.")))]
                          (s/join (map #(str "<option" (if (= % current) " selected=\"selected\">" ">") % "</option>") (range 31))))))
(parser/add-tag! :hoursopt (fn [args cmap]
                        (let [[cstr] args
                              current (get-in cmap (map keyword (s/split cstr #"\.")))]
                          (s/join (map #(str "<option" (if (= % current) " selected=\"selected\">" ">") % "</option>") (range 24))))))
(parser/add-tag! :cache (fn [args cmap content]
                          (let [key (s/join (map #(or (get-in cmap (map keyword (s/split % #"\."))) %) (butlast args)))
                                timeout (Integer/parseInt (last args))]
                            (if-let [content (util/get-item key)]
                              content
                              (let [content (get-in content [:cache :content])]
                                (util/save-item! key content timeout)
                                content))))
                 :endcache)

(deftype RenderableTemplate [template params]
  Renderable
  (render [_ request]
    (let [user (session/get :user)]
      (content-type
        (->> (assoc params
                    (if (re-matches #"/?admin/.+" template)
                      :admin-selected
                      (keyword (s/replace template #".html" "-selected"))) "active"
                    :dev (env :dev)
                    :user user
                    :site-tag (session/get :site-tag)
                    :is-site-admin (= (:role user) "site-admin")
                    :is-admin (= (:role user) "admin")
                    :is-user (= (:role user) "user")
                    :is-site-user (= (:role user) "site-user")
                    :is-evaluator (= (:role user) "evaluator")
                    :is-health-clerk  (= (:role user) "health-clerk")
                    :is-global (some #{(:role user)} ["admin" "user" "evaluator"])
                    :servlet-context
                    (if-let [context (:servlet-context request)]
                      (try (.getContextPath context)
                           (catch IllegalArgumentException _ context))))
          (parser/render-file (str template-path template))
          response)
        "text/html; charset=utf-8"))))

(defn render-file [template & [params]]
  (RenderableTemplate. template params))

(defn render
  ([text]
   (RenderableTemplate. "text.html" {:content text}))
  ([text params]
   (RenderableTemplate. "text.html" (assoc params :content text))))
