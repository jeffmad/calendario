(ns calendario.calusers
  (:require [yesql.core :refer [defquery]]))

(defquery user-by-email "queries/user-by-email.sql")
(defquery reset-calendar! "queries/reset-calendar.sql")
(defquery add-calendar<! "queries/add-calendar.sql")
(defquery next-calendar-id "queries/next-calendar-id.sql")
(defquery associate-cal-to-user<! "queries/assoc-cal-to-user.sql")
(defquery latest-calendar-text-for-user "queries/latest-calendar-text-for-user.sql")
(defquery add-expuser<! "queries/add-expuser.sql")
(defquery add-siteuser<! "queries/add-siteuser.sql")
(defquery expuser-by-email "queries/expuser-by-email.sql")
(defquery siteuser-by-iduser "queries/siteuser-by-iduser-siteid.sql")
(defquery uuid-by-email-siteid "queries/uuid-by-email-siteid.sql")
(defquery check-user-exists "queries/check-user-exists.sql")

; does siteuser have a calendar and if so, is it too old?
; get latest cal - return nothing or old one, or good one, then decide.

; https://www.google.com/calendar/ical/jeffmad%40gmail.com/private-6e3b43617c56c81ece0d93886ec3800d/basic.ics

(defn get-private-calendar-url [db email siteid]
  (let [uuid (:calid (first (uuid-by-email-siteid {:email email :siteid siteid} {:connection db})))
        enc-email (java.net.URLEncoder/encode email)]
    (if uuid
      (str "/calendar/ical/" enc-email "/private-" uuid "/trips.ics"))))

;
; calendars: idcalendar iduser icaltext createdate
; calendarsusers idcalendar iduser createdate
; expusers iduser  expuserid email  createdate
; siteusers idsiteuser iduser calid  tpid eapid tuid siteid  createdate
(defn calendar-for-user [tpid tuid now]
  (str "BEGIN: VCAL" tuid " " tpid " " now "END: VCAL"))

(defn find-expuser [db email]
  (first (expuser-by-email { :email email } {:connection db})))

; (c/find-siteuser (:spec (:db system)) 1)
(defn find-siteuser [db iduser siteid]
  (first (siteuser-by-iduser { :iduser iduser :siteid siteid} {:connection db})))

(defn user-lookup [db email uuid]
  (first (check-user-exists {:uuid uuid :email email} {:connection db})))

; (java.time.Instant/now)
(defn create-exp-user! [db expuserid email now]
  (add-expuser<! {:expuserid expuserid :email email :createdate (java.sql.Timestamp/from now)} {:connection db}))

(defn create-site-user! [db iduser uuid tpid eapid tuid siteid now]
  (add-siteuser<! {:iduser iduser :calid uuid :tpid tpid :eapid eapid :tuid tuid :siteid siteid :createdate (java.sql.Timestamp/from now)} {:connection db}))

;(java.util.UUID/randomUUID) (java.time.Instant/now)
(defn create-user [db expuserid email tpid eapid tuid siteid uuid now]
  (if-let [expuser (find-expuser db email)]
    (if-let [siteuser (find-siteuser db (:iduser expuser) siteid)]
      { :expuser expuser :siteuser siteuser}
      { :expuser expuser :siteuser  (create-site-user! db (:iduser expuser) uuid tpid eapid tuid siteid now)})
    (let [expuser (create-exp-user! db expuserid email now)]
      { :expuser expuser :siteuser
       (create-site-user! db (:iduser expuser) uuid tpid eapid tuid siteid now)})))

;(java.time.Instant/now)
(defn add-calendar-for-user! [db icaltext idsiteuser now]
  (let [now (java.sql.Timestamp/from now)
        idcal (:nextval (first  (next-calendar-id {} {:connection db})))]
    (if idcal
      (clojure.java.jdbc/with-db-transaction [t-con db]
        (add-calendar<! {:idcalendar idcal :icaltext icaltext :createdate now} {:connection t-con})
        (associate-cal-to-user<! {:idcalendar idcal :idsiteuser idsiteuser :createdate now} {:connection  t-con}))
      (throw (Exception. (str  "could not add calendar for user: " idsiteuser))))))

(defn expired? [expire-time create-time]
  (pos? (compare expire-time create-time)))

(def valid? (complement expired?))

#_(let [cal (calendar-for-user tpid tuid now)]
  (add-calendar-for-user! db cal idsiteuser now)
  cal)
;(java.time.Instant/now)
(defn latest-calendar-for-user [db email uuid expire-time]
  (if (user-lookup db email uuid)
    (let [{:keys [icaltext createdate idsiteuser tpid tuid]} (first (latest-calendar-text-for-user {:email email :calid uuid} {:connection db}))]
      (if (and icaltext (valid? expire-time (.toInstant createdate)))
        icaltext
        :expired))))
#_(let [c (c/latest-calendar-for-user (:spec (:db system)) "jeffmad@gmail.com" "6eb7f25d-4bbf-4753-aae2-72635fcd959b" (.plusSeconds (java.time.Instant/now) (* 60 60 -24)))] (cond (= :expired c) "yo expired" :else c))
(defn reset-private-calendar! [db email uuid]
  (let [expuser (find-expuser db email)]
    (when expuser
      (reset-calendar! {:uuid uuid :email email} {:connection db}))))
