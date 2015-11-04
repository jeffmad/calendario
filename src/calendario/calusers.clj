(ns calendario.calusers
  (:require [yesql.core :refer [defquery]]))

(defquery reset-calendar! "queries/reset-calendar.sql")
(defquery add-calendar<! "queries/add-calendar.sql")
(defquery next-calendar-id "queries/next-calendar-id.sql")
(defquery associate-cal-to-user<! "queries/assoc-cal-to-user.sql")
(defquery latest-calendar-text-for-user "queries/latest-calendar-text-for-user.sql")
(defquery latest-calendar-created-for-user "queries/latest-calendar-createdate-for-user.sql")
(defquery add-expuser<! "queries/add-expuser.sql")
(defquery add-siteuser<! "queries/add-siteuser.sql")
(defquery expuser-by-siteid-tuid "queries/expuser-by-siteid-tuid.sql")
(defquery siteusers-by-iduser "queries/siteusers-by-iduser.sql")
(defquery siteuser-by-siteid-tuid "queries/siteuser-by-siteid-tuid.sql")
(defquery check-user-exists "queries/check-user-exists.sql")
(defquery calendar-accessed-recently "queries/check-calendar-accessed.sql")
(defquery record-calendar-access<! "queries/record-calendar-access.sql")
(defquery find-active-users-with-expiring-calendars "queries/calendars-that-will-expire.sql")
;
; calendars: idcalendar iduser icaltext createdate
; calendarsusers idcalendar iduser createdate
; expusers iduser  expuserid email  createdate
; siteusers idsiteuser iduser calid  tpid eapid tuid siteid  createdate

(defn calendar-was-recently-accessed? [db siteid tuid]
  (> (:count (first (calendar-accessed-recently {:siteid siteid :tuid tuid} {:connection db}))) 0))

(defn record-calendar-access-by-user! [db idsiteuser now]
  (record-calendar-access<! {:idsiteuser idsiteuser :now (java.sql.Timestamp/from now)} {:connection db}))

(defn users-need-fresh-calendars [db]
  (vec (find-active-users-with-expiring-calendars {} {:connection db})))

(defn find-expuser-by-siteid-tuid [db siteid tuid]
  (first (expuser-by-siteid-tuid {:siteid siteid :tuid tuid} {:connection db})))

(defn find-siteusers-by-iduser [db iduser]
  (vec (siteusers-by-iduser { :iduser iduser } {:connection db})))

(defn find-siteuser [db siteid tuid]
  (first (siteuser-by-siteid-tuid { :tuid tuid :siteid siteid} {:connection db})))

(defn user-lookup [db email uuid]
  (first (check-user-exists {:uuid uuid :email email} {:connection db})))

; (java.time.Instant/now)
(defn create-exp-user! [db expuserid email now]
  (add-expuser<! {:expuserid expuserid :email email :createdate (java.sql.Timestamp/from now)} {:connection db}))

(defn create-site-user! [db iduser uuid tpid eapid tuid siteid now]
  (add-siteuser<! {:iduser iduser :calid uuid :tpid tpid :eapid eapid :tuid tuid :siteid siteid :createdate (java.sql.Timestamp/from now)} {:connection db}))

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

(defn record-access-if-not-present [db idsiteuser siteid tuid]
  (when idsiteuser
    (if-not (calendar-was-recently-accessed? db siteid tuid)
      (record-calendar-access-by-user! db idsiteuser (java.time.Instant/now)))))

(defn is-latest-calendar-older-than? [db idsiteuser ts]
  (let [{:keys [createdate]} (first (latest-calendar-created-for-user {:idsiteuser idsiteuser} {:connection db}))]
    (and createdate (neg? (compare (.toInstant createdate) ts)))))

;(java.time.Instant/now)
(defn latest-calendar-for-user [db email uuid expire-time]
  (if (user-lookup db email uuid)
    (let [{:keys [icaltext createdate idsiteuser siteid tuid]} (first (latest-calendar-text-for-user {:email email :calid uuid} {:connection db}))
          _ (record-access-if-not-present db idsiteuser siteid tuid)]
      (if (and icaltext (valid? expire-time (.toInstant createdate)))
        icaltext
        :expired))))

#_(let [c (c/latest-calendar-for-user (:spec (:db system)) "jeffmad@gmail.com" "6eb7f25d-4bbf-4753-aae2-72635fcd959b" (.plusSeconds (java.time.Instant/now) (* 60 60 -24)))] (cond (= :expired c) "yo expired" :else c))

(defn reset-private-calendar! [db siteid tuid uuid]
  (if (find-siteuser db siteid tuid)
   (reset-calendar! {:uuid uuid :siteid siteid :tuid tuid} {:connection db})))
