(defproject ckc "0.9.0-SNAPSHOT"
  :description "Alameda County Healthcare case management system (no longer used)"
  :ring {:handler ckc.handler/app, :init ckc.handler/init, :destroy ckc.handler/destroy}
  :ragtime {:migrations ragtime.sql.files/migrations, :database "jdbc:postgresql://localhost/ckc?user=ckc&password=ckc"}
  :plugins [[lein-ring "0.8.13"]
            [lein-environ "1.0.0"]
            [lein-ancient "0.5.5"]
            [ragtime/ragtime.lein "0.3.8"]]
  :url "http://ckc.noeticlogic.com/"
  :profiles {:uberjar {:omit-source false
             :env {:production true}
             :aot :all},
             :production {:ring {:open-browser? false, :stacktraces? false, :auto-reload? false}},
             :dev {:dependencies [[ring-mock "0.1.5"]
                                  [ring/ring-devel "1.3.2"]
                                  [pjstadig/humane-test-output "0.7.0"]],
                   :injections [(require 'pjstadig.humane-test-output)
                                (pjstadig.humane-test-output/activate!)],
                   :ring {:stacktrace-middleware prone.middleware/wrap-exceptions}
                   :env {:dev true}}}
  :main ckc.core
  :uberjar-name "ckc.jar"
  :jvm-opts ["-server" "-Xms1g" "-Xmx1g"]
  :target-path "target/%s/"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [log4j "1.2.17" :exclusions [javax.mail/mail javax.jms/jms com.sun.jdmk/jmxtools com.sun.jmx/jmxri]]
                 [http-kit "2.1.19"]
                 [ring "1.3.2"] ; added to deal with old versions being required by other libs
                 [markdown-clj "0.9.65" :exclusions [com.keminglabs/cljx]]
                 [prone "0.8.1"]
                 [noir-exception "0.2.3"]
                 [org.clojure/java.jdbc "0.3.6"] ; added explicit dependency to resolve issue with ragtime assuming 0.2
                 [com.taoensso/encore "1.22.0"] ; used by others. declared here to override deps issue
                 [com.taoensso/timbre "3.4.0"]
                 [selmer "0.8.2"]
                 [com.taoensso/tower "3.0.2"]
                 [com.taoensso/carmine "2.9.0"]
                 [danlentz/clj-uuid "0.1.5"]
                 [environ "1.0.0"]
                 [ring-server "0.4.0"]
                 [org.postgresql/postgresql "9.3-1102-jdbc41"]
                 [yesql "0.5.0-rc2"]
                 [com.mchange/c3p0 "0.9.5"]
                 [ragtime "0.3.8"]
                 [lib-noir "0.9.5"]
                 [im.chit/cronj "1.4.3"]
                 [cheshire "5.4.0"]
                 [twilio-api "1.0.0"]
                 [clj-time "0.9.0"]
                 [clj-http "1.0.1"]
                 [instaparse "1.3.5"]
                 [org.clojure/data.csv "0.1.2"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 ;[metrics-clojure "2.4.0"] ; see http://metrics-clojure.readthedocs.org/en/latest/ for docs
                 [me.raynes/conch "0.8.0"]]
  :repl-options  {:init-ns ckc.repl}
  :min-lein-version "2.0.0")
