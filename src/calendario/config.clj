(ns calendario.config
  (:require [environ.core :refer [env]]))

(def defaults
  ^:displace {:http {:port 3000}})

(def environ
  {:http {:port (some-> env :port Integer.)}
   :http-client {:timeout (some-> env :persistent-thread-timeout Integer.)
                 :threads (some-> env :http-connection-mgr-thread-count Integer.)
                 :default-per-route (some-> env :persistent-thread-default-per-route Integer.)
                 :socket-timeout (some-> env :http-client-socket-timeout Integer.)
                 :conn-timeout (some-> env :http-client-connection-timeout Integer.)
                 :user-service-endpoint (some-> env :user-service-endpoint)
                 :trip-service-endpoint (some-> env :trip-service-endpoint)}
   :db   {:uri  (some-> env :database-url)
          :conn-timeout (some-> env :db-conn-timeout Integer.)
          :pool-name (some-> env :pool-name)
          :username (some-> env :db-username)
          :password (some-> env :db-password)}
   :scheduler {:interval (some-> env :scheduler-interval Integer.)}
   :metrics {:host (some-> env :statsd-host)
             :port (some-> env :statsd-port Integer.)
             :reporting-interval (some-> env :statsd-interval Integer.)}
   :calendar-service {:expires-in-hours (some-> env :expires-in-hours Integer.)
                      :net-pool-size (some-> env :net-pool-size Integer.)}
  })
