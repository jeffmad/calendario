(defproject calendario "0.1.0"
  :description "web app that generates a user icalendar subscription"
  :url "http://ewegithub.sb.karmalab.net/EWE/calendario"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [com.stuartsierra/component "0.3.1"]
                 [compojure "1.4.0"]
                 [duct "0.5.8"]
                 [duct/hikaricp-component "0.1.0" :exclusions [org.slf4j/slf4j-nop]]
                 [environ "1.0.2"]
                 [meta-merge "0.1.1"]
                 [ring "1.4.0"]
                 [ring/ring-defaults "0.1.5"]
                 [ring-jetty-component "0.3.0"]
                 [duct/hikaricp-component "0.1.0"]
                 [yesql "0.5.2"]
                 [ring/ring-json "0.4.0"]
                 [org.postgresql/postgresql "9.4.1208"]
                 [clj-http "2.1.0"]
                 [cheshire "5.5.0"]
                 [clj-icalendar "0.1.2"]
                 [slingshot "0.12.2"]
                 [org.slf4j/log4j-over-slf4j "1.7.18"]
                 [org.slf4j/jcl-over-slf4j "1.7.18"]
                 [org.slf4j/jul-to-slf4j "1.7.18"]
                 [ch.qos.logback/logback-classic "1.1.6"]
                 [org.clojure/tools.logging "0.3.1"]
                 [org.clojure/data.zip "0.1.1"]
                 [org.clojure/data.xml "0.0.8"]
                 [overtone/at-at "1.2.0"]
                 [com.climate/claypoole "1.1.2"]
                 [prismatic/schema "1.0.5"]
                 [metrics-clojure "2.6.1"]
                 [metrics-clojure-jvm "2.6.1"]
                 [metrics-clojure-ring "2.6.1"]
                 [com.sun.jersey/jersey-core "1.19"] ;; jersey needed as a dependency of platform-isactive
                 [com.sun.jersey/jersey-servlet "1.19"]
                 [com.expedia.www.platform/platform-isactive "1.1.12"]]
  :plugins [[lein-environ "1.0.1"]
            [lein-gen "0.2.2"]]
  :repositories [["central" {:name "Expedia Central Nexus"
                             :url "http://nexus.sb.karmalab.net/nexus/content/groups/public"
                             :snapshots false}]]
  :generators [[duct/generators "0.4.5"][lein-gen/generators "0.2.2"]]
  :duct {:ns-prefix calendario}
  :main ^:skip-aot calendario.main
  :target-path "target/%s/"
  :aliases {"gen"   ["generate"]

            "setup" ["do" ["generate" "locals"]]}
  :uberjar-name "calendario-service.jar"
  :profiles
  {:dev  [:project/dev  :profiles/dev]
   :test [:project/test :profiles/test]
   :uberjar {:aot :all}
   :profiles/dev  {}
   :profiles/test {}
   :project/dev   {:source-paths ["dev"]
                   :repl-options {:init-ns user}
                   :dependencies [[reloaded.repl "0.2.1"]
                                  [org.clojure/tools.namespace "0.2.11"]
                                  [eftest "0.1.0"]
                                  [kerodon "0.7.0"]]
                   :env {:port 3000}
                   :jvm-opts ["-Dapplication.name=prime-sample-clojure" ;;these properties needed for platform-isactive and log4j
                              "-Dapplication.home=."
                              "-Dapplication.environment=dev"]}
   :project/test  {}}
   :jvm-opts ["-Dcom.sun.management.jmxremote.authenticate=false
              -Dcom.sun.management.jmxremote.ssl=false
              -Dcom.sun.management.jmxremote.port=1098
              -Dcom.sun.management.jmxremote.rmi.port=1098"])
