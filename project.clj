(defproject calendario "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [com.stuartsierra/component "0.3.0"]
                 [compojure "1.4.0"]
                 [duct "0.4.5"]
                 [duct/hikaricp-component "0.1.0" :exclusions [org.slf4j/slf4j-nop]]
                 [environ "1.0.1"]
                 [meta-merge "0.1.1"]
                 [ring "1.4.0"]
                 [ring/ring-defaults "0.1.5"]
                 [ring-jetty-component "0.3.0"]
                 [duct/hikaricp-component "0.1.0"]
                 [yesql "0.5.1"]
                 [ring/ring-json "0.4.0"]
                 [org.postgresql/postgresql "9.4-1203-jdbc4"]
                 [clj-http "2.0.0"]
                 [cheshire "5.5.0"]
                 [clj-icalendar "0.1.2"]
                 [slingshot "0.12.2"]
                 [org.slf4j/log4j-over-slf4j "1.7.12"]
                 [org.slf4j/jcl-over-slf4j "1.7.12"]
                 [org.slf4j/jul-to-slf4j "1.7.12"]
                 [ch.qos.logback/logback-classic "1.1.3"]
                 [org.clojure/tools.logging "0.3.1"]
                 [org.clojure/data.zip "0.1.1"]
                 [org.clojure/data.xml "0.0.8"]
                 [overtone/at-at "1.2.0"]
                 [com.climate/claypoole "1.1.0"]
                 [prismatic/schema "1.0.3"]
                 [metrics-clojure "2.6.0"]
                 [metrics-clojure-jvm "2.6.0"]
                 [metrics-clojure-ring "2.6.0"]
                 ]
  :plugins [[lein-environ "1.0.1"]
            [lein-gen "0.2.2"]]
  :generators [[duct/generators "0.4.5"][lein-gen/generators "0.2.2"]]
  :duct {:ns-prefix calendario}
  :main ^:skip-aot calendario.main
  :target-path "target/%s/"
  :aliases {"gen"   ["generate"]
            "setup" ["do" ["generate" "locals"]]}
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
                   :env {:port 3000}}
   :project/test  {}})
