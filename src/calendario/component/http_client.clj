(ns calendario.component.http-client
  (:require [com.stuartsierra.component :as component]
            [clj-http.client :as client]))

(defn- make-config [{:keys [timeout threads default-per-route]}]
  { :timeout (or timeout 10)
   :threads (or threads 10)
   :default-per-route (or default-per-route 5)
   :insecure? true})

(defrecord HttpClient [timeout threads default-per-route socket-timeout conn-timeout user-service-endpoint trip-service-endpoint]
  component/Lifecycle
  (start [this]
    (if (:conn-mgr this)
      this
      (assoc this :conn-mgr (clj-http.conn-mgr/make-reusable-conn-manager (make-config this)))))
  (stop [this]
    (if-let [conn-mgr (:conn-mgr this)]
      (do (clj-http.conn-mgr/shutdown-manager conn-mgr)
          (dissoc this :conn-mgr)))))

(defn http-client [options]
  (map->HttpClient options))
