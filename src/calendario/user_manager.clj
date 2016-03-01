(ns calendario.user-manager
  (:require [calendario.calusers :as cu]
            [calendario.user-service :as us]
            [clojure.tools.logging :refer [error warn debug]]))

(defn- add-siteuser!
  "add a site user."
  [db iduser uuid tpid tuid siteid locale now]
  (cu/create-site-user! db iduser uuid tpid 0 tuid siteid locale now))

(defn- create-expuser! [db
                        expuserid
                        email
                        now]
  (cu/create-exp-user! db expuserid email now))

(defn user-lookup
  "take siteid / tuid and verify user exists in database. if user exists,
   return map of user, expuser along with the siteusers. if user does not exist,
   return nil. "
  [db siteid tuid]
  (when (cu/find-site-user db siteid tuid)
    (let [expuser (cu/find-expuser-by-siteid-tuid db siteid tuid)]
      {:expuser expuser
       :siteusers (cu/find-siteusers-by-iduser db (:iduser expuser))})))


(defn- find-or-create-expuser [db
                               email
                               expuserid
                               now]
  (if-let [expuser (cu/find-expuser-by-email db email)]
    expuser
    (create-expuser! db
                     expuserid
                     email
                     now)))

; _ (debug (str "user profile site:" siteid " tuid:" tuid " " user-profile))

(defn create-siteusers! [db
                         user-accounts
                         locale
                         iduser
                         now]
  (let [eligible (filter #(= "false" (:single-use %)) (:tuidmappings user-accounts))
        non-existing (filter
                      #(nil? (cu/find-site-user db
                                                (Integer/parseInt (:tpid %))
                                                (Integer/parseInt (:tuid %)))) eligible)
        _ (debug (str "non-existing: iduser:" iduser " count: " (count non-existing)))]
    (mapv #(add-siteuser! db
                          iduser
                          (java.util.UUID/randomUUID)
                          (Integer/parseInt (:tpid %))
                          (Integer/parseInt (:tuid %))
                          (Integer/parseInt (:tpid %)) ; TODO: use tpid siteid translation
                          locale
                          now) non-existing)))

(def up-memo (memoize us/get-user-profile))
(def ue-memo (memoize us/get-user-by-email))

(defn single-use-account? [user-accounts siteid tuid]
  (seq (filter #(and (= tuid (:tuid %))
                     (= siteid (:tpid %))
                     (= "true" (:single-use %)))
               (:tuidmappings user-accounts))))

(defn create-user
  "call user service to get profile, then call user service to get all
   siteid/tuids for that email. Finally create the users in the
   database."
  [db http-client metrics-registry siteid tuid]
  (let [;user-profile (up-memo http-client metrics-registry siteid tuid)
        user-profile (us/get-user-profile http-client metrics-registry siteid tuid)
        email (:email user-profile)
        locale (:locale user-profile)
        now (java.time.Instant/now)
        ;user-accounts (ue-memo http-client metrics-registry email)
        user-accounts (us/get-user-by-email http-client metrics-registry email)
        _ (debug (str "user accounts site:" siteid " tuid:" tuid " " user-accounts))]
    (if (single-use-account? user-accounts siteid tuid)
      (let [msg (str  "no calendar for single use account"
                      " siteid: " siteid " tuid: " tuid)]
        (error msg)
        (throw (ex-info msg
                        {:cause :service-unavailable
                         :error msg})))
      (let [expuser (find-or-create-expuser db
                                            email
                                            (Integer/parseInt (:expuserid user-accounts))
                                            now)
            _ (debug (str "expuser is:" expuser))
            _ (create-siteusers! db user-accounts locale (:iduser expuser) now)]
        (user-lookup db siteid tuid)))))
