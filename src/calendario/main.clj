(ns calendario.main
  (:gen-class)
  (:require [com.stuartsierra.component :as component]
            [duct.middleware.errors :refer [wrap-hide-errors]]
            [meta-merge.core :refer [meta-merge]]
            [calendario.config :as config]
            [calendario.system :refer [new-system]]
            [clojure.tools.logging.impl :refer [find-factory get-logger]])
  (:import (org.slf4j.bridge SLF4JBridgeHandler)))

(defn bridge-jul->slf4j
  "Redirects all Java.util.logging logs to sl4fj. Should be called
  upon application startup"
  []
  (SLF4JBridgeHandler/removeHandlersForRootLogger)
  (SLF4JBridgeHandler/install))

(defn set-default-uncaught-exception-handler
  "Installs a default exception handler to log any exception which is
  neither caught by a try/catch nor captured as the result of a
  Future. Should be called upon application startup"
  [logger]
  (Thread/setDefaultUncaughtExceptionHandler
   (reify Thread$UncaughtExceptionHandler
     (uncaughtException [_ thread throwable]
       (.error logger "Uncaught exception" throwable)))))

(def prod-config
  {:app {:middleware     [[wrap-hide-errors :internal-error]]
         :internal-error "Internal Server Error"}})

(def config
  (meta-merge config/defaults
              config/environ
              prod-config))

(defn -main [& args]
  (let [system (new-system config)]
    (bridge-jul->slf4j)
    (set-default-uncaught-exception-handler (get-logger (find-factory) *ns*))
    (println "Starting HTTP server on port" (-> system :http :port))
    (component/start system)))
