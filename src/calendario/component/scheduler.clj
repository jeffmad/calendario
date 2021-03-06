(ns calendario.component.scheduler
  (:require [com.stuartsierra.component :as component]
            [overtone.at-at :refer [mk-pool stop-and-reset-pool! show-schedule stop interspaced every]]
            [clojure.tools.logging :refer [error warn debug]]
            [calendario.component.calendar-service :refer [refresh-stale-calendars]]))

(defrecord Scheduler [interval calendar-service]
  component/Lifecycle
  (start [this]
    (let [pool (fn [m]
                 (if (:pool m)
                   m
                   (assoc m :pool (mk-pool :cpu-count 1))))
          refresh-stale (partial refresh-stale-calendars calendar-service)
          job  (fn [m]
                 (if (:job m)
                   m
                   (assoc m :job (interspaced interval
                                              refresh-stale
                                              (:pool m)
                                              :fixed-delay true
                                              :initial-delay 5000))))]
      (-> this
          pool
          job)))

  (stop [this]
    (let [stop-job (fn [m]
                     (if (:job m)
                       (stop (:job m)))
                     (assoc m :job nil))
          _ (debug "now stopping timer job")]
      (-> this
          stop-job
          (assoc :pool nil)))))

(defn scheduler [options]
  (map->Scheduler options))
