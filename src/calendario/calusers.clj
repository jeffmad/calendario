(ns calendario.calusers
  (:require [yesql.core :refer [defquery]]))

(defquery user-by-expuserid "queries/user-by-expuserid.sql")
(defquery reset-calendar! "queries/reset-calendar.sql")
(defquery create-calendar-user<! "queries/create-calendar-user.sql")
(defquery add-calendar<! "queries/add-calendar.sql")
(defquery associate-cal-to-user<! "queries/assoc-cal-to-user.sql")
; https://www.google.com/calendar/ical/jeffmad%40gmail.com/private-6e3b43617c56c81ece0d93886ec3800d/basic.ics
; email doesn't work for us because one can change it and i don't want to call userprofile
; expuserid-tpid-tuid ? or expuserid-siteid?
; get latest calendar for user
; select icaltext from caluser."calusers" u, cal."calendars" c, cal."calendarsusers" cu where u.expuserid = :expuserid and u.tpid = :tpid and u.tuid = :tuid and u.calid = :uuid and cu.iduser = u.iduser and cu.idcalendar = c.idcalendar and
; or look at the hostname look up tpid tuid from platform site, then use email

;select date_trunc('MONTH', DATE '2015-10-12');
;select date_trunc('MONTH', CURRENT_DATE);
;select idcalendar from cal."calendars" where createdate >= date_trunc('month', current_date) and createdate < date_trunc('month', current_date);



(defn get-private-calendar-url [db expuserid]
  (user-by-expuserid {:expuserid expuserid} db))

(defn reset-private-calendar [db expuserid]
  (let [uuid (java.util.UUID/randomUUID)
        rs (reset-calendar-url-for-user! {:uuid uuid :expuserid expuserid} db)]
   uuid))
; calendars: idcalendar iduser icaltext createdate
; calendarsusers idcalendar iduser createdate
; calusers iduser calid expuserid tpid eapid tuid siteid email parentiduser createdate

(defn create-user [db uuid expuserid tpid eapid tuid siteid email createdate]
  (create-calendar-user<! {:calid uuid :expuserid expuserid :tpid tpid :eapid eapid :tuid tuid :siteid siteid :email email :createdate createdate} db))

;(c/create-user {:connection (:spec (:db system))} (java.util.UUID/randomUUID) 577015 1 0 577015 1 "jmadynski@expedia.com" (java.sql.Timestamp/from (java.time.Instant/now)))

                                        ;(c/create-user {:connection (:spec (:db system))} (java.util.UUID/randomUUID) 577016 1 0 577016 1 "jmadynski2@expedia.com" (java.sql.Timestamp/from (java.time.Instant/now)))
                                        ;{:email "jmadynski2@expedia.com", :eapid 0, :iduser 3, :siteid 1, :expuserid 577016, :tuid 577016, :calid #uuid "e29dbeb5-2625-4cc5-9e4e-c06d0d9d79ef", :parentiduser nil, :tpid 1, :createdate #inst "2015-10-14T08:02:44.764000000-00:00"}
