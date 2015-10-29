(ns calendario.component.calendar-service
  (:require [com.stuartsierra.component :as component]
            [calendario.calusers :refer [reset-private-calendar! create-exp-user! create-site-user! latest-calendar-for-user user-lookup add-calendar-for-user! user-lookup]]
            [calendario.user-manager :as um]
            [calendario.trip-fetcher :as tf]
            [calendario.calendar :as c]))

(defrecord CalendarService [expires-in-hours db http-client]
  component/Lifecycle
  (start [this] this)
  (stop [this] this))

(defn calendar-service [options]
  (map->CalendarService options))

; {{db :spec} db http-client :http-client}
(defn get-calendar-url-for-user
  "given a siteid and tuid, return a url for the user's calendar.
   Anyone who has this url can see the user's booked upcoming trips.
   If the user has shared the url with others and no longer wants
   those individuals to have access, the user must reset the calendar url."
  [{{db :spec} :db http-client :http-client} siteid tuid]
  (let [u (or (um/user-lookup db siteid tuid)
              (um/create-user db http-client siteid tuid))
        email (get-in u [:expuser :email])
        uuid (:calid (first (filter #(= tuid (:tuid %)) (:siteusers u))))]
    (str "/calendar/ical/" (java.net.URLEncoder/encode email) "/private-" uuid "/trips.ics")))

(defn make-user
  "take input json and create a user in the database.
   normal users will not use this. It is a utility for testing
   or manual."
  [{{db :spec} :db http-client :http-client} m]
  (let [now (java.time.Instant/now)
        siteid (:siteid m)
        tuid (:tuid m)
        expuser (create-exp-user! db (:expuserid m) (:email m) now)
        siteuser (create-site-user! db (:iduser expuser) (java.util.UUID/randomUUID) (:tpid m) (:eapid m) tuid siteid now)]
    (when (and expuser siteuser)
     {:siteid siteid :tuid tuid})))

(defn reset-calendar-for-user
  "given a siteid and tuid and new uuid, lookup verify that the
   user exists, then reset their uuid to the uuid input param. "
  [{{db :spec} :db http-client :http-client} siteid tuid uuid]
  (and (um/user-lookup db siteid tuid)
       (= 1 (reset-private-calendar! db siteid tuid uuid))))

(defn time-n-hours-ago
  "given an intger input representing hours, return the
   current time GMT minus the input hours"
  [hours]
  (let [h (if (pos? hours) (-' hours) hours)]
    (.plusSeconds (java.time.Instant/now) (* 60 60 h))))

(defn build-and-store-latest-calendar
  "pull the booked upcoming trips, make calendar events for them,
   and store the calendar text in the database"
  [{{db :spec} :db http-client :http-client} email uuid]
  (let [user (user-lookup db email uuid)
        idsiteuser (:idsiteuser user)
        cal-text (->> (tf/get-booked-upcoming-trips http-client (:tuid user) (:siteid user))
                      (tf/get-json-trips http-client (:tuid user) (:siteid user))
                      c/calendar-from-json-trips)
        _ (add-calendar-for-user! db cal-text idsiteuser (java.time.Instant/now))]
    cal-text))

(def empty-cal "BEGIN:VCALENDAR
PRODID:-//Expedia, Inc. //Trip Calendar V0.1//EN
VERSION:2.0
METHOD:PUBLISH
CALSCALE:GREGORIAN
END:VCALENDAR")

(defn build-or-get-cached-calendar
  "get the latest calendar for the user. If it is expired, then build and store
   a new calendar, then return it"
  [{{db :spec} :db http-client :http-client :as calendar-service} email uuid expire-time]
  (let [latest (latest-calendar-for-user db email uuid expire-time)]
    (if (= :expired latest)
      (build-and-store-latest-calendar calendar-service email uuid)
      latest)))

; rebuild every calendar that has been accessed between >23 and <25 hours ago and
; is expired

(defn calendar-for
  "given an email and a guid token, return the cached or recently built
   calendar "
  [{{db :spec} :db http-client :http-client :as calendar-service} email token]
  (let [uuid (second (re-matches #"^private-([a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12})" token))
        expire-time (time-n-hours-ago (:expires-in-hours calendar-service))
        user (user-lookup db email uuid)]
    (if user
      (build-or-get-cached-calendar calendar-service email uuid expire-time)
      empty-cal)))
