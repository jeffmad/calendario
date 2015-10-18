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

;
; calendars: idcalendar iduser icaltext createdate
; calendarsusers idcalendar iduser createdate
; expusers iduser  expuserid email  createdate
; siteusers idsiteuser iduser calid  tpid eapid tuid siteid  createdate
(defn calendar-for-user [tpid tuid]
  (str "BEGIN: VCAL" tuid " " tpid " " (java.time.Instant/now) "END: VCAL"))

(defn find-expuser [db email]
  (first (expuser-by-email { :email email } {:connection db})))

; (c/find-siteuser (:spec (:db system)) 1)
(defn find-siteuser [db iduser siteid]
  (first (siteuser-by-iduser { :iduser iduser :siteid siteid} {:connection db})))

(defn user-lookup [db email uuid]
  (first (check-user-exists {:uuid uuid :email email} {:connection db})))

(defn create-exp-user! [db expuserid email]
  (add-expuser<! {:expuserid expuserid :email email :createdate (java.sql.Timestamp/from (java.time.Instant/now))} {:connection db}))

(defn create-site-user! [db iduser uuid tpid eapid tuid siteid ]
  (add-siteuser<! {:iduser iduser :calid uuid :tpid tpid :eapid eapid :tuid tuid :siteid siteid :createdate (java.sql.Timestamp/from (java.time.Instant/now))} {:connection db}))

(defn create-user [db expuserid email tpid eapid tuid siteid]
  (if-let [expuser (find-expuser db email)]
    (if-let [siteuser (find-siteuser db (:iduser expuser) siteid)]
      { :expuser expuser :siteuser siteuser}
      { :expuser expuser :siteuser  (create-site-user! db (:iduser expuser) (java.util.UUID/randomUUID) tpid eapid tuid siteid)})
    (let [expuser (create-exp-user! db expuserid email)]
      { :expuser expuser :siteuser
       (create-site-user! db (:iduser expuser) (java.util.UUID/randomUUID) tpid eapid tuid siteid)})))


(defn add-calendar-for-user! [db icaltext idsiteuser]
  (let [now (java.sql.Timestamp/from (java.time.Instant/now))
        idcal (:nextval (first  (next-calendar-id {} {:connection db})))]
    (if idcal
      (clojure.java.jdbc/with-db-transaction [t-con db]
        (add-calendar<! {:idcalendar idcal :icaltext icaltext :createdate now} {:connection t-con})
        (associate-cal-to-user<! {:idcalendar idcal :idsiteuser idsiteuser :createdate now} {:connection  t-con}))
      (throw (Exception. (str  "could not add calendar for user: " idsiteuser))))))

(defn latest-calendar-for-user [db email uuid]
  (if (user-lookup db email uuid)
    (let [{:keys [icaltext createdate idsiteuser tpid tuid]} (first (latest-calendar-text-for-user {:email email :calid uuid} {:connection  db}))]
      (if (and icaltext (neg? (compare (.plusSeconds (java.time.Instant/now) (* 60 60 -24)) (.toInstant createdate))))
        icaltext
        (let [cal (calendar-for-user tpid tuid)]
          (add-calendar-for-user! db cal idsiteuser)
          cal)))))

(defn reset-private-calendar! [db email]
  (let [expuser (find-expuser db email)
        uuid (java.util.UUID/randomUUID)]
    (when expuser
      (reset-calendar! {:uuid uuid :email email} {:connection db}))))
