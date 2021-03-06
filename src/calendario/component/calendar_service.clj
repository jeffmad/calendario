(ns calendario.component.calendar-service
  (:require [com.stuartsierra.component :as component]
            [calendario.calusers :as cu]
            [calendario.user-manager :as um]
            [calendario.trip-fetcher :as tf]
            [calendario.calendar :as c]
            [com.climate.claypoole :as cp]
            [clojure.tools.logging :refer [error warn debug]]
            [schema.core :as s]
            [metrics.counters :refer [inc! counter]]
            [metrics.histograms :refer [update! histogram]]))

; CURRENT_DATE - (? || ' days')::interval
; > (CURRENT_DATE - ?::interval)
(defrecord CalendarService [expires-in-hours net-pool-size db http-client metrics]
  component/Lifecycle
  (start [this]
    (let [add-net-pool (fn [m]
                         (if (:net-pool m)
                           m
                           (assoc m :net-pool (cp/threadpool net-pool-size))))
          add-cpu-pool (fn [m] (if (:cpu-pool m)
                                 m
                                 (assoc m :cpu-pool (cp/threadpool (cp/ncpus)))))]
      (-> this
          add-net-pool
          add-cpu-pool)))

  (stop [this]
    (let [remove-net-pool (fn [m] (if (:net-pool m)
                                    (do (cp/shutdown (:net-pool m))
                                        (dissoc m :net-pool))
                                    m))
          remove-cpu-pool (fn [m] (if (:cpu-pool m)
                                    (do (cp/shutdown (:cpu-pool m))
                                        (dissoc m :cpu-pool))
                                    m))]
      (-> this
          remove-net-pool
          remove-cpu-pool))))

(defn calendar-service [options]
  (map->CalendarService options))

; {{db :spec} db http-client :http-client}
(defn get-calendar-url-for-user
  "given a siteid and tuid, return a url for the user's calendar.
   Anyone who has this url can see the user's booked upcoming trips.
   If the user has shared the url with others and no longer wants
   those individuals to have access, the user must reset the calendar url."
  [{{db :spec} :db http-client :http-client {reg :registry} :metrics} siteid tuid]
  (let [_ (debug (str "Getting calendar url for user with siteid " siteid " tuid:" tuid))
        u (or (um/user-lookup db siteid tuid)
              (um/create-user db http-client reg siteid tuid))
        _ (debug (str siteid "/" tuid " " u))
        expuserid (:expuserid (:expuser u))
        uuid (:calid (first (filter #(= tuid (:tuid %)) (:siteusers u))))]
    (if (and expuserid uuid)
      (str "/calendar/ical/"
           expuserid "/private-" uuid "/trips.ics")
      (throw (ex-info (str  "unable to get calendar url for user tuid:"
                            tuid " siteid: " siteid)
                      {:cause :service-unavailable
                       :error (str  "unable to get expuserid and uuid for user with tuid: " tuid " siteid: " siteid) })))))

;curl -v -k -H "Content-Type: application/json" -X POST -d '{"expuserid": 600000, "email": "kurt@vonnegut.com", "tpid": 1, "eapid": 0, "tuid": 550000, "siteid": 1}' 'http://localhost:3000/api/user'

(s/defschema User
  {:expuserid s/Int
   :email s/Str
   :tpid  s/Int
   :eapid s/Int
   :tuid s/Int
   :siteid s/Int
   })
#_(defmacro try* [& body] `(try (do ~@body) (catch Exception e# e#)))
#_(defn validate [schema data] (try* (schema/validate schema data)))
; this will return nil if all good or something if there is an :error key

; { :expuserid 17 :email "a@b.com" :tpid 1 :eapid 0 :tuid 577015 :siteid 1}
(defn validate-user
  "take a user map as input, validate it against the schema, and return nil
   if it is valid or return the error if it is not valid"
  [d]
  (if-let [error (some->
                  (try (s/validate User d)
                       (catch clojure.lang.ExceptionInfo e
                         (-> e ex-data))) :error)]
    error
    nil))
(defn- create-user [{{db :spec} :db http-client :http-client} m]
  (let [_ (debug "creating a user: " m)
        now (java.time.Instant/now)
        siteid (:siteid m)
        tuid (:tuid m)
        locale (:locale m)
        expuser (cu/create-exp-user! db (:expuserid m) (:email m) now)
        siteuser (cu/create-site-user! db (:iduser expuser) (java.util.UUID/randomUUID) (:tpid m) (:eapid m) tuid siteid locale now)]
    (when (and expuser siteuser)
      {:siteid siteid :tuid tuid})))

(defn make-user
  "take input json and create a user in the database.
   normal users will not use this. It is a utility for testing
   or manual."
  [{{db :spec} :db http-client :http-client :as calendar-service} m]
  (if-let [error (validate-user m)]
    (throw (ex-info
            "cannot create user, validation failed"
            {:cause :malformed-request
             :error (str error)}))
    (create-user calendar-service m)))

(defn reset-calendar-for-user
  "given a siteid and tuid and new uuid, lookup verify that the
   user exists, then reset their uuid to the uuid input param. "
  [{{db :spec} :db http-client :http-client} siteid tuid uuid]
  (and (um/user-lookup db siteid tuid)
       (= 1 (cu/reset-private-calendar! db siteid tuid uuid))))

; is it worth it to keep an atom here to prevent generating calendar while it is being generated?
(defn build-and-store-calendar-for-user
  "pull the booked upcoming trips, make calendar events for them,
   and store the calendar text in the database"
  [{{db :spec} :db http-client :http-client {reg :registry} :metrics net-pool :net-pool cpu-pool :cpu-pool} idsiteuser siteid tuid]
  (let [_ (debug (str "building and storing a cal for tuid " tuid " siteid " siteid))
        upcoming-trips (tf/get-booked-upcoming-trips http-client reg tuid siteid)
        trip-f (partial tf/get-trip-for-user http-client reg tuid siteid)
        trip-jsons (cp/upmap net-pool trip-f upcoming-trips)
        events (cp/upmap cpu-pool c/create-events-for-trip (remove nil? trip-jsons))
        _ (debug (str "created " (count events) " calendar events for " tuid " siteid " siteid))
        cal-text (c/calendar-from-events (mapcat seq events))
        ;cal-text (->> (tf/get-booked-upcoming-trips http-client tuid siteid)
        ;              (tf/get-json-trips http-client tuid siteid)
        ;              c/calendar-from-json-trips)
        _ (cu/add-calendar-for-user! db cal-text idsiteuser (java.time.Instant/now))]
    cal-text))

(defn build-and-store-latest-calendar
  "pull the booked upcoming trips, make calendar events for them,
   and store the calendar text in the database"
  [{{db :spec} :db http-client :http-client :as calendar-service} expuserid uuid]
  (let [user (cu/user-lookup-by-expuserid db expuserid uuid)]
    (build-and-store-calendar-for-user calendar-service (:idsiteuser user) (:siteid user) (:tuid user))))

(defn time-n-hours-before
  "given an intger input representing hours, return the
   current time GMT minus the input hours"
  [start-time hours]
  (let [h (if (pos? hours) (-' hours) hours)]
    (.plusSeconds start-time (* 60 60 h))))

(defn refresh-stale-calendars
  "method to run from a periodic timer to refresh calendars that need a refresh.
   It needs a try catch to make sure nothing bad happens with the timer, and if
   something does, the error gets logged."
  [{{db :spec} :db
    net-pool :net-pool
    {registry :registry} :metrics :as calendar-service}]
  (try
    (when (cu/get-advisory-lock? db)
      (let [users (cu/users-need-fresh-calendars db)
            _ (debug (str "found " (count users) " with recent access"))
            stale-users-f (partial cu/is-latest-calendar-older-than? db)
            stales (set (filter #(stale-users-f (:idsiteuser %) (time-n-hours-before (java.time.Instant/now) 20)) users))
            _ (debug (str (count stales) " users have stale calendars"))
            _ (update! (histogram registry ["calendario" "trip" "stalefound"]) (count stales))
            build (partial build-and-store-calendar-for-user calendar-service)]
        (when (seq stales)
          (do
            (debug "now beginning preemptive update of stale calendars.")
            (doall (cp/upmap net-pool #(build (:idsiteuser %) (:siteid %) (:tuid %)) stales))
            (debug "finished preemptively updating stale calendars.")))))
    (catch Exception ex
      (error ex "caught exception while refreshing stale calendars"))
    (finally (cu/release-advisory-lock db))))

(defn build-or-get-cached-calendar
  "get the latest calendar for the user. If it is expired, then build and store
   a new calendar, then return it"
  [{{db :spec} :db http-client :http-client :as calendar-service} expuserid uuid expire-time]
  (let [latest (cu/latest-calendar-for-user db expuserid uuid expire-time)]
    (if (= :expired latest)
      (build-and-store-latest-calendar calendar-service expuserid uuid)
      latest)))

; rebuild every calendar that has been accessed between >23 and <25 hours ago and
; is expired

(defn calendar-for
  "given an expuserid and a guid token, return the cached or recently built
   calendar "
  [{{db :spec} :db http-client :http-client :as calendar-service} expuserid token]
  (let [uuid (second (re-matches #"^private-([a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12})" token))
        expire-time (time-n-hours-before (java.time.Instant/now) (:expires-in-hours calendar-service))
        user (cu/user-lookup-by-expuserid db expuserid uuid)]
    (if user
      (build-or-get-cached-calendar calendar-service expuserid uuid expire-time))))
