(ns user
  (:require [clojure.repl :refer :all]
            [clojure.pprint :refer [pprint]]
            [clojure.tools.namespace.repl :refer [refresh]]
            [clojure.java.io :as io]
            [com.stuartsierra.component :as component]
            [eftest.runner :as eftest]
            [meta-merge.core :refer [meta-merge]]
            [reloaded.repl :refer [system init start stop go reset]]
            [ring.middleware.stacktrace :refer [wrap-stacktrace]]
            [calendario.config :as config]
            [calendario.system :as system]))

(def dev-config
  {:app {:middleware [wrap-stacktrace]}
   :http-client {:timeout 10
                 :threads 10
                 :default-per-route 4
                 :socket-timeout 60000
                 :conn-timeout 1000
                 :user-service-endpoint "https://userservicev3.integration.karmalab.net:56783"
                 :trip-service-endpoint "http://wwwexpediacom.integration.sb.karmalab.net"}
   :db {:uri "jdbc:postgresql://localhost/caldb"
        :conn-timeout 10000
        }
   :scheduler {:interval (* 1000 60 5)}
   :calendar-service {:expires-in-hours 8}})

(def config
  (meta-merge config/defaults
              config/environ
              dev-config))

(defn new-system []
  (into (system/new-system config)
        {}))

(ns-unmap *ns* 'test)

(defn test []
  (eftest/run-tests (eftest/find-tests "test") {:multithread? false}))

(when (io/resource "local.clj")
  (load "local"))

(reloaded.repl/set-init! new-system)
