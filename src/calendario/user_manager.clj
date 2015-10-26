(ns calendario.user-manager
  (:require [calendario.calusers :as cu]
            [calendario.user-service :as us]))

(defn add-siteuser! [db iduser tpid tuid siteid now]
  (cu/create-site-user! db iduser (java.util.UUID/randomUUID) tpid 0 tuid siteid now))

(defn create-user [db http-client siteid tuid]
  (let [up (us/get-user-profile "https://userservicev3.integration.karmalab.net:56783" siteid tuid)
        email (:email up)
        user-accounts (us/get-user-by-email "https://userservicev3.integration.karmalab.net:56783" email)
        now (java.time.Instant/now)
        expuser (cu/create-exp-user! db (Integer/parseInt (:expuserid user-accounts)) email now)
        iduser (:iduser expuser)
        siteusers (mapv #(add-siteuser! db iduser (Integer/parseInt (:tpid %)) (Integer/parseInt (:tuid %)) siteid now) (:tuidmappings user-accounts))]
    { :expuser expuser
     :siteusers siteusers}))

(defn user-lookup [db siteid tuid]
  (if-let [su (cu/find-siteuser db siteid tuid)]
    (let [expuser (cu/find-expuser-by-siteid-tuid db siteid tuid)]
      {
       :expuser expuser
       :siteusers (cu/find-siteusers-by-iduser db (:iduser expuser))
       })))
