(ns ckc.util
  (:require [noir.io :as io]
            [taoensso.tower :as tower]
            [me.raynes.conch :as conch]
            [clojure.string :as s]
            [markdown.core :as md]
            [selmer.parser :as p]
            [instaparse.core :as insta]
            [clojure.edn]
            [ckc.db.core :as db])
  (:use [taoensso.timbre :only [trace debug info warn error fatal]]))

(defn md->html
  "reads a markdown file from public/md and returns an HTML string"
  [filename]
  (md/md-to-html-string (io/slurp-resource filename) :heading-anchors true))

; Replace markdown-clj's make-heading function to add
; the "anchor" class to the "a" element it creates so that
; I can style it to push below the fixed navbar
(intern 'markdown.transformers 'make-heading
        (fn [text heading-anchors]
          (if-let [heading (markdown.transformers/heading-level text)]
            (let [text (markdown.transformers/heading-text text)]
              (str "<h" heading ">"
                (if heading-anchors
                  (str "<a class=\"anchor\" name=\""
                       (-> text clojure.string/lower-case (clojure.string/replace " " "&#95;")) "\"></a>"))
                text "</h" heading ">")))))

; Handy way to convert "truthy" values into true, and "falsy" ones to false
(def !! (comp not not))

; Handy way to "comment out" some forms. Just surround with "nop"
(defmacro nop [& body] nil)

(defn parse-param [str]
  (let [fp #"^([-+]?[0-9]+(\.[0-9]*)?([eE][-+]?[0-9]+)?)$"
        ; Full int matcher: ip #"^([-+]?)(?:(0)|([1-9][0-9]*)|0[xX]([0-9A-Fa-f]+)|0([0-7]+)|([1-9][0-9]?)[rR]([0-9A-Za-z]+)|0[0-9]+)(N)?$"
        ip #"^[-+]?(0|[1-9][0-9]*)$"
        isint (re-matches ip str)]
    (if isint
      (Long/parseLong (first isint))
      (let [isfp (re-matches fp str)]
        (if isfp
          (Double/parseDouble str)
          str)))))

(defn uuid []
  (.toString (java.util.UUID/randomUUID)))

(defn now []
  (.getTime (new java.util.Date)))

; Basic timed cache; useful for dealing with file download issue (see mailings)
(defonce saved-items (atom {}))

(defn save-item! [id content howlong]
  (let [tmout (+ (* 1000 howlong) (now))]
    (swap! ckc.util/saved-items assoc id {:content content :timeout tmout})))

(defn get-item [id]
  (if-let [item (get @ckc.util/saved-items id)]
    (if (> (:timeout item) (now))
      (:content item)
      (do
        (swap! ckc.util/saved-items dissoc id)
        nil))))

(defn clear-item! [id]
  (swap! ckc.util/saved-items dissoc id))

(defn has-item? [id]
  (let [item (get @ckc.util/saved-items id)]
    (and item (> (:timeout item) (now)))))

(defonce no-translations [:tl :fa :km :hy :hmn])

(defn fmt [loc x & [style]]
  (let [kloc (keyword loc)
        tloc (if (some #{kloc} no-translations) :en kloc)]
    (tower/fmt tloc x style)))

(defn clip
  "Clips the provided string to at most n characters"
  [s n]
  (subs s 0 (min (count s) n)))

(defn clip-word
  "Clips the provided string s to the nearest word boundary less than or equal to n"
  [s n]
  (cond
    (<= (count s) n) s
    (Character/isWhitespace (.charAt s n)) (subs s 0 n)
    :else (loop [o (dec n)]
            (cond
              (< o 0) (clip s n)
              (Character/isWhitespace (.charAt s o))
                (loop [p (dec o)]
                  (cond
                    (< p 0) (clip s n)
                    (not (Character/isWhitespace (.charAt s p)))
                      (subs s 0 (inc p))
                    :else (recur (dec p))))
              :else (recur (dec o))))))

(defn html-to-docx [content filename]
  (let [tempfile (clojure.java.io/file "/tmp" filename)
        tfpath (.getAbsolutePath tempfile)]
    (try
      (conch/with-programs [pandoc] (pandoc "-s" "-f" "html" "-t" "docx" "-o" tfpath {:in content}))
      tempfile
      (catch clojure.lang.ExceptionInfo err
        (error (s/join "\n" (get-in (ex-data err) [:proc :err])))
        nil))))

(defn generate-vars [appt language is-html?]
  (let [locale (first (db/get-locale-for {:name language} {:row-fn :locale}))
        pgnames (s/join " & " (db/guardians-for {:family_id (:family_id appt)} {:row-fn :name}))]
      {:pg pgnames
       :date (fmt locale (:start_time appt) :date-long)
       :shortdate (fmt locale (:start_time appt) :date-short)
       :time (fmt locale (:start_time appt) :time-short)
       :duration (db/duration appt)
       :assister (:type appt)
       :address (if is-html?
                  (str "<p>" (:street1 appt) "<br/>" (:city appt) ", CA " (:zipcode appt) "</p>")
                  (str (:street1 appt) "\n" (:city appt) ", CA " (:zipcode appt)))}))

(defn render-template [appt template language]
  (let [template (or template "")
        language (or language "english")
        is-html? (!! (re-matches #".*<(.+)>.*</\1>.*" template))]
    (p/render template (generate-vars appt language is-html?))))

(defn deep-update-in
  "Looks for key 'k' anywhere within map 'm' and replaces its value with the result
  of calling function 'f' on it. (Note, f can also be a constant value, which simply
  replaces the current value of the key outright)."
  [m k f]
  (letfn [(find-key [ks k m]
                    (cond (map? m)
                          (reduce into (map (partial conj ks) (filter #{k} (keys m)))
                                  (map #(find-key (conj ks (key %)) k (val %)) m))
                          (vector? m)
                          (reduce into '() (map #(find-key (conj ks %1) k %2)
                                                (iterate inc 0) m))))]
    (let [f (if (fn? f) f (constantly f))]
      (reduce #(update-in %1 %2 f) m (find-key [] k m)))))

(def arithmetic
  (insta/parser
    "expr = add-sub
     <add-sub> = mul-div | add | sub
     add = add-sub <'+'> mul-div
     sub = add-sub <'-'> mul-div
     <mul-div> = term | mul | div
     mul = mul-div <'*'> term
     div = mul-div <'/'> term
     <term> = number | <'('> add-sub <')'>
     number = #'[0-9]+(\\.[0-9]+)?'"))

(defn calc [expr]
  (->> (arithmetic expr)
     (insta/transform
       {:add +, :sub -, :mul *, :div /,
        :number clojure.edn/read-string :expr identity})))
