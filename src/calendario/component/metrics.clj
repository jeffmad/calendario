(ns calendario.component.metrics
  (:require [com.stuartsierra.component :as component]
            [metrics.core :refer [new-registry remove-all-metrics]]
            [metrics.jvm.core :refer [instrument-jvm]]
            [metrics.reporters.console :as r]
            [metrics.counters :refer [defcounter]]
            [metrics.histograms :refer [defhistogram]])
  (:import java.util.concurrent.TimeUnit
           ;[com.bealetech.metrics.reporting Statsd StatsdReporter]
           [com.codahale.metrics MetricFilter]))

#_(defn statsd-reporter [registry host port]
  (let [statsd (Statsd. host port)
        reporter (StatsdReporter/forRegistry registry)]
    (-> reporter
        .prefixedWith "cal"
        .convertDurationsTo TimeUnit/MILLISECONDS
        .convertRatesTo TimeUnit/SECONDS
        .filter MetricFilter/ALL
        .build statsd)))

(defn console-reporter [registry]
  (let [opts {:stream (java.io.PrintStream. "/Users/jmadynski/src/clojure/calendario/metrics/metrics.txt" )
              :rate-unit TimeUnit/SECONDS
              :duration-unit TimeUnit/MILLISECONDS
              :filter MetricFilter/ALL}]
    (r/reporter registry opts)))

;all jvm instrumentation - metrics.clj
;all api 200 40x 50x meters - calapi.clj
;counter - calendar access - calapi.clj
;counter - calendar reset - calapi.clj
;counter - get calendar url for user  - calapi.clj
;counter - success user profile - user_service.clj
;counter - fail user profile - user_service.clj
;counter - success get user accounts - user_service.clj
;counter - fail get user accounts - user_service.clj
;histogram - request time for get user profile - user_service.clj
;histogram - request time for get user accounts - user_service.clj
;counter - success trip summary - trip_fetcher.clj
;counter - fail trip summary - trip_fetcher.clj
;histogram - request time for get trip summary - trip_fetcher.clj;
;histogram - request time for get trip - trip_fetcher.clj
;counter - success trip detail - trip_fetcher.clj
;counter - fail trip detail - trip_fetcher.clj
;histogram - #calendars preemptively built - calendar_service.clj;

;histogram dbaccess - calusers.clj
;counter db error - calusers.clj



(defn init-metrics [registry]
  (defcounter registry ["calendario" "api" "calaccess"])
  (defcounter registry ["calendario" "api" "calreset"])
  (defcounter registry ["calendario" "api" "calurl"])
  (defcounter registry ["calendario" "us" "profaccess"])
  (defcounter registry ["calendario" "us" "proferror"])
  (defhistogram registry ["calendario" "us" "profiletime"])
  (defcounter registry ["calendario" "us" "acctsaccess"])
  (defcounter registry ["calendario" "us" "acctserror"])
  (defhistogram registry ["calendario" "us" "acctstime"])
  (defcounter registry ["calendario" "us" "readtimeout"])
  (defcounter registry ["calendario" "us" "connecttimeout"])
  (defcounter registry ["calendario" "trip" "sumsucc"])
  (defcounter registry ["calendario" "trip" "sumerr"])
  (defcounter registry ["calendario" "trip" "detsucc"])
  (defcounter registry ["calendario" "trip" "deterr"])
  (defhistogram registry ["calendario" "trip" "sumtime"])
  (defhistogram registry ["calendario" "trip" "dettime"])
  (defcounter registry ["calendario" "trip" "readtimeout"])
  (defcounter registry ["calendario" "trip" "connecttimeout"])
  (defhistogram registry ["calendario" "trip" "stalefound"])
  )

(defrecord Metrics [host port reporting-interval]
  component/Lifecycle
  (start [this]
    (let [reg (new-registry)
          ;reporter (statsd-reporter reg host port)
          reporter (console-reporter reg)]
      (instrument-jvm reg)
      (init-metrics reg)
      (r/start reporter reporting-interval)
    (assoc this :registry reg :reporter reporter)))
  (stop [this]
    (if-let [r (:reporter this)]
      (r/stop r))
    (if-let [reg (:registry this)]
      (remove-all-metrics reg))
    (dissoc this :registry :reporter)))

(defn metrics [options]
  (map->Metrics options))
