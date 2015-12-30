(defproject calendario-service "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [com.stuartsierra/component "0.3.0"]
                 [compojure "1.4.0"]
                 [duct "0.5.3"]
                 [environ "1.0.1"]
                 [meta-merge "0.1.1"]
                 [ring "1.4.0"]
                 [ring/ring-defaults "0.1.5"]
                 [ring/ring-json "0.4.0"]
                 [ring-jetty-component "0.3.0"]
                 [org.slf4j/slf4j-log4j12 "1.7.12"]
                 [org.clojure/tools.logging "0.3.1"]

                 [com.sun.jersey/jersey-core "1.17.1"] ;; jersey needed as a dependency of platform-isactive
                 [com.sun.jersey/jersey-servlet "1.17.1"]
                 [com.expedia.www.platform/platform-isactive "1.1.5"]]
  :plugins [[lein-environ "1.0.1"]
            [lein-gen "0.2.2"]]
  :repositories [["central" {:name "Expedia Central Nexus"
                             :url "http://nexus.sb.karmalab.net/nexus/content/groups/public"
                             :snapshots false}]]
  :generators [[duct/generators "0.5.3"]]
  :duct {:ns-prefix calendario-service}
  :main ^:skip-aot calendario-service.main
  :target-path "target/%s/"
  :aliases {"gen"   ["generate"]
            "setup" ["do" ["generate" "locals"]]}
  :uberjar-name "calendario-service.jar"
  :resources-path "resources"
  :profiles
  {:dev  [:project/dev  :profiles/dev]
   :test [:project/test :profiles/test]
   :uberjar {:aot :all}
   :profiles/dev  {}
   :profiles/test {}
   :project/dev   {:dependencies [[reloaded.repl "0.2.1"]
                                  [org.clojure/tools.namespace "0.2.11"]
                                  [org.clojure/tools.nrepl "0.2.12"]
                                  [eftest "0.1.0"]
                                  [kerodon "0.7.0"]
                                  [ring/ring-mock "0.3.0"]]
                   :source-paths ["dev"]
                   :repl-options {:init-ns user}
                   :env {:port 3000
                         :expedia-environment "dev"}
                   :jvm-opts ["-Dapplication.name=calendario-service" ;;these properties needed for platform-isactive and log4j
                              "-Dapplication.home=."
                              "-Dapplication.environment=dev"]}
   :project/test  {}})
