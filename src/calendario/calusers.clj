(ns calendario.calusers
  (:require [yesql.core :refer [defqueries]]
            [clojure.tools.logging :refer [error warn debug]]
            [metrics.counters :refer [inc! counter]]
            [metrics.histograms :refer [update! histogram]])
  (:import (org.postgresql.util PSQLException)
           (java.sql SQLException)))

(defqueries "queries/calendar-queries.sql")
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
        db-timeout #"Timeout after \d+ms of waiting for a connection."
        syntax-error #"syntax error at or near"
        duplicate-key #"duplicate key value violates unique constraint"
        foreign-key-constraint #"insert or update on table \"\w+\" violates foreign key constraint"
        non-null-constraint #"null value in column \"\w+\" violates not-null constraint"
        wrong-data-type #"cannot cast type \w+ to \w+"
        table-or-column-does-not-exist #"(?:relation|column) \"\w+\" (?:of relation )?does not exist"
        type-mismatch #"column \"\w+\" is of type \w+ but expression is of type"
        db-connection #"Connection to \w+:\d+ refused. Check that the hostname and port are correct and that the postmaster is accepting TCP/IP connections."]
    (when (re-find db-connection message)
      (throw
       (ex-info
        "Unable to connect to database."
        {:cause :service-unavailable})))
    (when (re-find type-mismatch message)
      (throw
       (ex-info
        "type mismatch in statement. you need to rewrite or cast the expression"
        {:cause :service-unavailable})))
    (when (re-find table-or-column-does-not-exist message)
      (throw
       (ex-info
        "table or column does not exist"
        {:cause :service-unavailable})))
    (when (re-find wrong-data-type message)
      (throw
       (ex-info
        "argument is not correct type, cannot cast"
        {:cause :service-unavailable})))
    (when (re-find non-null-constraint message)
      (throw
       (ex-info
        "Non null constraint violation"
        {:cause :service-unavailable})))
    (when (re-find foreign-key-constraint message)
      (throw
       (ex-info
        "Foreign key constraint violation"
        {:cause :service-unavailable})))
    (when (re-find db-timeout message)
      (throw
       (ex-info
        "Error: Timed out waiting for a database connection"
        {:cause :service-unavailable})))
    (when-let [[_ field] (re-find not-null message)]
      (throw
       (ex-info
        (format "%s field cannot be blank" field)
        {:cause :service-unavailable})))
    (when (re-find duplicate-key message)
      (throw
       (ex-info
        "duplicate key value violates unique key constraint"
        {:cause :resource-exists})))
    (when (re-find syntax-error message)
      (throw
       (ex-info
        "sql syntax error"
        {:cause :service-unavailable})))
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

(defn find-expuser-by-email
  "given an email, return a map representing the expuser. if not found,
   nil is returned."
  [db email]
  (first
   (try-pgsql
    (expuser-by-email {:email email}
                      {:connection db}))))

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

(defn find-site-user
  "given siteid and tuid, return a map representing the siteuser. if not found,
   nil is returned."[db siteid tuid]
  (first
   (try-pgsql
    (siteuser-by-siteid-tuid {:tuid tuid
                              :siteid siteid}
                             {:connection db}))))

(defn user-lookup-by-expuserid
  "given an expuserid and uuid, return a map representing the user.
   If an exp user has more than 1 siteuser, only the siteuser matching the uuid
   will be returned. If no user is found, nil is returned. "
  [db expuserid uuid]
  (first
   (try-pgsql
    (check-expuser-exists {:uuid uuid
                           :expuserid expuserid}
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

(defn add-calendar [t-con idcal icaltext idsiteuser now]
  (try-pgsql
   (let [now (java.sql.Timestamp/from now)]
     (add-calendar<! {:idcalendar idcal
                      :icaltext icaltext
                      :createdate now}
                     {:connection t-con})
     (associate-cal-to-user<! {:idcalendar idcal
                               :idsiteuser idsiteuser
                               :createdate now}
                              {:connection  t-con}))))

(defn next-calendar-pk [db]
  (-> (try-pgsql (next-calendar-id {} {:connection db}))
      first
      :nextval))

;(java.time.Instant/now)
(defn add-calendar-for-user!
  "insert 1 row into calendars table and 1 row into calendarsusers table to
   associate the calendar to the user it belongs to. "
  [db icaltext idsiteuser now]
  (try
    (if-let [idcal (next-calendar-pk db)]
      (clojure.java.jdbc/with-db-transaction [t-con db]
        (add-calendar t-con idcal icaltext idsiteuser now)))
    (catch Exception ex
      (error ex (str "could not add calendar for user: " idsiteuser)))))

(defn expired?
  "return true if the expire time is greater than
   the create time. "
  [expire-time create-time]
  (pos? (compare expire-time create-time)))

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
  [db expuserid uuid expire-time]
  (if (user-lookup-by-expuserid db expuserid uuid)
    (let [{:keys [icaltext
                  createdate
                  idsiteuser
                  siteid
                  tuid]} (first
                          (try-pgsql
                           (latest-calendar-text-for-user {:expuserid expuserid
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
  (if (find-site-user db siteid tuid)
    (try-pgsql
     (reset-calendar! {:uuid uuid
                       :siteid siteid
                       :tuid tuid}
                      {:connection db}))))

(defn get-advisory-lock? [db]
  (try-pgsql
   (-> (get-lock {} {:connection db})
       first
       :pg_try_advisory_lock)))

(defn release-advisory-lock [db]
  (try-pgsql
   (->  (release-lock {} {:connection db})
        first
        :pg_advisory_unlock)))
