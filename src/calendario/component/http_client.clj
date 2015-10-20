(ns calendario.component.http-client
  (:require [com.stuartsierra.component :as component]
            [clj-http.client :as client]))

(defn- make-config [{:keys [timeout threads]}]
  { :timeout (or timeout 5)
    :threads (or threads 5)})

(defrecord HttpClient [timeout threads]
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
