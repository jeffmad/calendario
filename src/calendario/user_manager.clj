(ns calendario.user-manager
  (:require [calendario.calusers :as cu]
            [calendario.user-service :as us]))

(defn- add-siteuser!
  "add a site user."
  [db iduser uuid tpid tuid siteid locale now]
  (cu/create-site-user! db iduser uuid tpid 0 tuid siteid locale now))

(defn create-user
  "call user service to get profile, then call user service to get all
   siteid/tuids for that email. Finally create the users in the
   database."
  [db http-client metrics-registry siteid tuid]
  (let [up (us/get-user-profile http-client metrics-registry siteid tuid)
        email (:email up)
        locale (:locale up)
        user-accounts (us/get-user-by-email http-client metrics-registry email)
        now (java.time.Instant/now)
        expuser (cu/create-exp-user!
                 db
                 (Integer/parseInt (:expuserid user-accounts)) email now)
        iduser (:iduser expuser)
        siteusers (mapv #(add-siteuser! db
                                        iduser
                                        (java.util.UUID/randomUUID)
                                        (Integer/parseInt (:tpid %))
                                        (Integer/parseInt (:tuid %))
                                        siteid
                                        locale
                                        now)
                        (:tuidmappings user-accounts))]
    { :expuser expuser
     :siteusers siteusers}))

(defn user-lookup
  "take siteid / tuid and verify user exists in database. if user exists,
   return map of user, expuser along with the siteusers. if user does not exist,
   return nil."
  [db siteid tuid]
  (if-let [su (cu/find-siteuser db siteid tuid)]
    (let [expuser (cu/find-expuser-by-siteid-tuid db siteid tuid)]
      {:expuser expuser
       :siteusers (cu/find-siteusers-by-iduser db (:iduser expuser))})))
