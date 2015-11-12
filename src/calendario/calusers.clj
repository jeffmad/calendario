(ns calendario.calusers
  (:require [yesql.core :refer [defquery]]
            [clojure.tools.logging :refer [error warn debug]])
  (:import (org.postgresql.util PSQLException)
           (java.sql SQLException)))

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
; siteusers idsiteuser iduser calid tpid eapid tuid siteid locale createdate

(defn handle-db-exception
  "this is called when a postgresql exception is thrown.
   A more generic ex-info will be thrown"
  [exception]
  (let [_ (error exception "Exception during database call")
        message (.getMessage exception)
        not-null #"null value in column \"(\w+)\" violates not-null constraint"
        positive-page-count #"new row for relation \"books\" violates check constraint \"positive_page_count\""]
    (when-let [[_ field] (re-find not-null message)]
      (throw
       (ex-info
        (format "%s field cannot be blank" field)
        {})))
    (when (re-find positive-page-count message)
      (throw
       (ex-info
        "Books must have a positive page count"
        {})))
    (throw exception)))

(defmacro try-pgsql
  [& body]
  `(try
     (do ~@body)
     (catch org.postgresql.util.PSQLException e#
       (handle-db-exception e#))
     (catch java.sql.SQLException e#
       (handle-db-exception e#))))

#_(try (clojure.java.jdbc/insert! (:spec (:db system)) :siteusers {:iduser 1 :calid "6eb7f25d-4bbf-4753-aae2-72635fcd959b" :tpid 1 :eapid 1 :tuid 577015 :siteid 1 :createdate (java.sql.Timestamp/from (java.time.Instant/now)) :locale "en_US" }) (catch org.postgresql.util.PSQLException e (print (str " ->" (.getMessage e) " <-"))))

(defn calendar-was-recently-accessed?
  "return true if calendar was recently accessed.
   the lastaccessed table is checked to determine if
   a row was inserted within the last 24 hours"
  [db siteid tuid]
  (-> (try-pgsql
       (calendar-accessed-recently {:siteid siteid
                                    :tuid tuid}
                                   {:connection db}))
      first
      :count
      (> 0)))

(defn record-calendar-access-by-user!
  "insert a row into the last access table to record the fact that the calendar
   is being accessed."
  [db idsiteuser now]
  (try-pgsql
   (record-calendar-access<! {:idsiteuser idsiteuser
                              :now (java.sql.Timestamp/from now)}
                             {:connection db})))

(defn users-need-fresh-calendars
  "return a list of maps containing keys siteid, tuid, idsiteuser for every user
   whose calendar will soon expire. To get this list a query is run to find
   users who have an entry in the last accessed table between 23hrs 45min ago
   and 23hrs 30min ago. If it is 11:45am when this query is run, the query will
   find users who have an entry in the lastaccessed table from yesterday 12 noon
   to yesterday 12:15pm. That gives us 15 min to rebuild all the calendars that
   will expire. An empty vector is returned if there are no calendars to build."
  [db]
  (vec
   (try-pgsql
    (find-active-users-with-expiring-calendars {} {:connection db}))))

(defn find-expuser-by-siteid-tuid
  "given a siteid and tuid, return a map representing the expuser. if not found,
   nil is returned."
  [db siteid tuid]
  (first
   (try-pgsql
    (expuser-by-siteid-tuid {:siteid siteid
                             :tuid tuid}
                            {:connection db}))))

(defn find-siteusers-by-iduser
  "given an iduser (pk from expusers table), return a list of the siteusers.
   an exp user can have accounts on many different points of sale. if not found,
   nil is returned."
  [db iduser]
  (vec
   (try-pgsql
    (siteusers-by-iduser {:iduser iduser}
                         {:connection db}))))

(defn find-siteuser
  "given siteid and tuid, return a map representing the siteuser. if not found,
   nil is returned."[db siteid tuid]
  (first
   (try-pgsql
    (siteuser-by-siteid-tuid {:tuid tuid
                              :siteid siteid}
                             {:connection db}))))

(defn user-lookup
  "given an email and uuid, return a map representing the user.
   If an exp user has more than 1 siteuser, only the siteuser matching the uuid
   will be returned. If no user is found, nil is returned. "
  [db email uuid]
  (first
   (try-pgsql
    (check-user-exists {:uuid uuid
                        :email email}
                       {:connection db}))))

; (java.time.Instant/now)
(defn create-exp-user!
  "insert a row in the expusers table based on the input args"
  [db expuserid email now]
  (try-pgsql
   (add-expuser<! {:expuserid expuserid
                   :email email
                   :createdate (java.sql.Timestamp/from now)}
                  {:connection db})))

(defn create-site-user!
  "insert a row into the siteusers table based in input args."
  [db iduser uuid tpid eapid tuid siteid locale now]
  (try-pgsql
   (add-siteuser<! {:iduser iduser
                    :calid uuid
                    :tpid tpid
                    :eapid eapid
                    :tuid tuid
                    :siteid siteid
                    :locale locale
                    :createdate (java.sql.Timestamp/from now)}
                   {:connection db})))

;(java.time.Instant/now)
(defn add-calendar-for-user!
  "insert 1 row into calendars table and 1 row into calendarsusers table to
   associate the calendar to the user it belongs to. "
  [db icaltext idsiteuser now]
  (let [now (java.sql.Timestamp/from now)
        idcal (-> (try-pgsql (next-calendar-id {} {:connection db}))
                  first
                  :nextval)]
    (if idcal
      (clojure.java.jdbc/with-db-transaction [t-con db]
        (try-pgsql
         (add-calendar<! {:idcalendar idcal
                          :icaltext icaltext
                          :createdate now}
                         {:connection t-con})
         (associate-cal-to-user<! {:idcalendar idcal
                                   :idsiteuser idsiteuser
                                   :createdate now}
                                  {:connection  t-con})))
      (throw (Exception. (str "could not add calendar for user: " idsiteuser))))))

(defn expired?
  "return true if the expire time is less than
   the create time. "
  [expire-time create-time]
  (neg? (compare expire-time create-time)))

(def valid? (complement expired?))

(defn record-access-if-not-present
  "insert a row to the last accessed table if if there is not already a recent
   entry"
  [db idsiteuser siteid tuid]
  (when idsiteuser
    (if-not (calendar-was-recently-accessed? db siteid tuid)
      (record-calendar-access-by-user! db idsiteuser (java.time.Instant/now)))))

(defn is-latest-calendar-older-than?
  "take in a timestamp, lookup the latest calendar for the user,
   then check if the calendar is older than the timestamp"
  [db idsiteuser ts]
  (let [{:keys [createdate]} (first
                              (try-pgsql
                               (latest-calendar-created-for-user {:idsiteuser idsiteuser}
                                                                 {:connection db})))]
    (and createdate
         (neg? (compare (.toInstant createdate) ts)))))

(defn latest-calendar-for-user
  "if the user exists, lookup and return the latest calendar for the user.
   side effect is to record the fact that it was accessed if not already present.
   If the calendar is expired return :expired"
  [db email uuid expire-time]
  (if (user-lookup db email uuid)
    (let [{:keys [icaltext
                  createdate
                  idsiteuser
                  siteid
                  tuid]} (first
                          (try-pgsql
                           (latest-calendar-text-for-user {:email email
                                                           :calid uuid}
                                                          {:connection db})))
          _ (record-access-if-not-present db idsiteuser siteid tuid)]
      (if (and icaltext
               (valid? expire-time (.toInstant createdate)))
        icaltext
        :expired))))

#_(let [c (c/latest-calendar-for-user (:spec (:db system)) "jeffmad@gmail.com" "6eb7f25d-4bbf-4753-aae2-72635fcd959b" (.plusSeconds (java.time.Instant/now) (* 60 60 -24)))] (cond (= :expired c) "yo expired" :else c))

(defn reset-private-calendar!
  "assign this user a new uuid for their calendar perhaps because they no longer
   want to share the calendar with others"
  [db siteid tuid uuid]
  (if (find-siteuser db siteid tuid)
    (try-pgsql
     (reset-calendar! {:uuid uuid
                       :siteid siteid
                       :tuid tuid}
                      {:connection db}))))
