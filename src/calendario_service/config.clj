(ns calendario-service.config
  (:require [environ.core :refer [env]]))

(def defaults
   {:http ^:displace {:port 3000}
    :hello {:message "Hello world!"}})

(def environ
  {:http {:port (some-> env :port Integer.)}})

(def expedia-environ
  (case (env :expedia-environment)
    "dev" {:hello {:message "Hello dev!"}}
    "test" {:hello {:message "Hello test!"}}
    "int" {:hello {:message "Hello int!"}}
    "prod" {:hello {:message "Hello prod!"}}
    {}))