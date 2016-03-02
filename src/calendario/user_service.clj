(ns calendario.user-service
  (:require [clj-http.client :as client]
            [clojure.tools.logging :refer [log error warn debug]]
            [clojure.data.xml :as xml]
            [clojure.zip :as zip]
            [clojure.data.zip.xml :as zx]
            [slingshot.slingshot :refer [try+ throw+]]
            [metrics.counters :refer [inc! counter]]
            [metrics.histograms :refer [update! histogram]]
            ))

(defn post-url [url {:keys [throw-exceptions
                            body
                            content-type
                            accept
                            insecure?
                            conn-timeout
                            socket-timeout
                            conn-mgr]} meta-data metrics-registry]
  (try+
   (client/post url {:throw-exceptions throw-exceptions
                     :body body
                     :content-type content-type
                     :accept accept
                     :insecure? insecure?
                     :conn-timeout conn-timeout
                     :socket-timeout socket-timeout
                     :connection-manager conn-mgr })
   (catch java.net.SocketTimeoutException ste
     (error ste (str "socket timeout reading " url))
     (inc! (counter metrics-registry ["calendario" "us" "readtimeout"]))
     (throw+ {:cause :service-unavailable
              :error {:reason (.getMessage ste)
                      :data meta-data
                      :url url}}
             "encountered socket timeout"))
   (catch java.net.ConnectException ce
     (error ce (str "connect exception to url " url))
     (inc! (counter metrics-registry ["calendario" "us" "connecttimeout"]))
     (throw+ {:cause :service-unavailable
              :error {:reason (.getMessage ce)
                      :data meta-data
                      :url url}}
             "encountered connection exception"))
   (catch Object _
     (error (:throwable &throw-context) "unexpected error")
     (throw+))))

(def exp-account-template "<?xml version=\"1.0\" encoding=\"utf-8\"?><usr:listTUIDsForExpAccountRequest xmlns:usr=\"urn:com:expedia:www:services:user:messages:v3\" siteID=\"1\"><usr:expUser emailAddress=\"%s\" /><usr:messageInfo enableTraceLog=\"false\" clientName=\"localhost\" transactionGUID=\"a2192179-d5b7-4234-918c-8f662aaaf545\"/></usr:listTUIDsForExpAccountRequest>")

(defn- tuid-mapping [t]
  {:tpid (zx/xml1-> t (zx/attr :tpid))
   :tuid (zx/xml1-> t (zx/attr :tuid))
   :single-use (zx/xml1-> t (zx/attr :singleUse))})

(defn- accounts [r]
  (let [x (xml/parse (java.io.StringReader. r))
        z (zip/xml-zip x)]
    (if (= "true" (zx/xml1-> z (zx/attr :success)))
      { :expuserid (zx/xml1-> z :expUser (zx/attr :id))
       :email (zx/xml1-> z :expUser (zx/attr :emailAddress))
       :tuidmappings (mapv tuid-mapping (zx/xml-> z :expUserTUIDMapping))}
      (error (str "user accounts response not successful " r)))))

(defn update-metrics-user-by-email-success! [metrics-registry time]
  (inc! (counter metrics-registry ["calendario" "us" "acctsaccess"]))
  (update! (histogram metrics-registry ["calendario" "us" "acctstime"]) time))

(defn update-metrics-user-by-email-error! [metrics-registry]
  (inc! (counter metrics-registry ["calendario" "us" "acctserror"])))

(defn lru-cache [max-size]
  (proxy [java.util.LinkedHashMap] [16 0.75 true]
    (removeEldestEntry [entry]
      (> (count this) max-size))))




;{:expuserid "301078", :email "jmadynski@expedia.com", :tuidmappings [{:tpid "1", :tuid "5363093", :single-use "true"} {:tpid "1", :tuid "577015", :single-use "false"}]}
; "https://userservicev3.integration.karmalab.net:56783"


(defn read-user-accounts
  "given an http client and email address, call user service to retrieve
   the siteid / tuid combinations that this email address has. If there
   is a problem messaging the user service, an exception is thrown. If the
   xml response does not indicate success = true, nil is returned. If the http
   status of the response is not 200, nil is returned. "
  [{:keys [user-service-endpoint
           conn-timeout
           socket-timeout
           conn-mgr]} metrics-registry email]
  {:pre [(and (not (clojure.string/blank? email))
              ((complement nil?) email))]}
  (let [url (str user-service-endpoint "/exp-account/tuids")
        resp (post-url url {:throw-exceptions true
                            :body (format exp-account-template email)
                            :content-type :xml
                            :accept :xml
                            :insecure? true
                            :conn-timeout conn-timeout
                            :socket-timeout socket-timeout
                            :connection-manager conn-mgr }
                       {:data {:email email}}
                       metrics-registry)]
    (if (= 200 (:status resp))
      (do
        (update-metrics-user-by-email-success! metrics-registry (:request-time resp))
        (accounts (:body resp)))
      (do
        (update-metrics-user-by-email-error! metrics-registry)
        (throw (ex-info (str  "could not get accounts for user, status="
                              (:status resp) " email: " email
                              " response:" resp )
                        {:cause :service-unavailable
                         :error "did not get status 200 retrieving user accounts" }))))))

(def user-account-cache (lru-cache 32))

(defn user-account-from-cache [email]
  (get user-account-cache email))

(defn get-user-by-email [http-client metrics-registry email]
  (if-let [u (user-account-from-cache email)]
    u
    (let [user (read-user-accounts http-client metrics-registry email)]
      (when user
        (.put user-account-cache email user)
        user))))


(defn- profile
  "take the raw xml response, parse it, and return a map containing the required
   fields."
  [r]
  (let [x (xml/parse (java.io.StringReader. r))
        z (zip/xml-zip x)]
    (if (= "true" (zx/xml1-> z (zx/attr :success)))
      { :email (zx/xml1-> z :user (zx/attr :emailAddress))
       :locale (zx/xml1-> z :user (zx/attr :locale))
       :first (zx/xml1-> z :user :personalName (zx/attr :first))
       :last (zx/xml1-> z :user :personalName (zx/attr :last))
       :country-code (zx/xml1-> z :user :preferredPhone (zx/attr :countryCode))
       :phone (zx/xml1-> z :user :preferredPhone (zx/attr :phoneNumber))}
      (throw (ex-info (str  "profile response not successful, status="
                            (:status r) " response: " r)
                      {:cause :service-unavailable
                       :error "did not get success in xml attr in profile" })))))

(def exp-profile-template "<?xml version=\"1.0\" encoding=\"utf-8\"?><usr:getUserProfileRequest xmlns:usr=\"urn:com:expedia:www:services:user:messages:v3\" siteID=\"%s\"><usr:user actAsTuid=\"%s\" loggedInTuid=\"%s\"/><usr:messageInfo enableTraceLog=\"false\" clientName=\"localhost\" transactionGUID=\"a2192179-d5b7-4234-918c-8f662aaaf545\"/></usr:getUserProfileRequest>")

(defn update-metrics-user-profile-success! [metrics-registry time]
  (inc! (counter metrics-registry ["calendario" "us" "profaccess"]))
  (update! (histogram metrics-registry ["calendario" "us" "profiletime"]) time))

(defn update-metrics-user-profile-error! [metrics-registry]
  (inc! (counter metrics-registry ["calendario" "us" "proferror"])))

;"https://userservicev3.integration.karmalab.net:56783"
(defn read-user-profile
  "given http client and siteid tuid, return profile. An exception will be
   thrown if there is a problem exchanging messages with user service.
   If the xml response from user service indicates success = false,
   nil is returned."
  [{:keys [user-service-endpoint
           conn-timeout
           socket-timeout
           conn-mgr]} metrics-registry site-id tuid]
  (let [url (str user-service-endpoint "/profile/get")
        resp (post-url url {:throw-exceptions true
                            :body (format exp-profile-template site-id tuid tuid)
                            :content-type :xml
                            :accept :xml
                            :insecure? true
                            :conn-timeout conn-timeout
                            :socket-timeout socket-timeout
                            :connection-manager conn-mgr }
                       {:data {:siteid site-id
                        :tuid tuid}} metrics-registry)]
    (if (= 200 (:status resp))
      (do
        (update-metrics-user-profile-success! metrics-registry (:request-time resp))
        (profile (:body resp)))
      (do
        (update-metrics-user-profile-error! metrics-registry)
        (throw (ex-info (str  "could not get profile for user, status="
                              (:status resp) " tuid: " tuid
                              " siteid: " site-id " response:" resp )
                        {:cause :service-unavailable
                         :error "did not get status 200 retrieving user profile" }))))))

(def user-profile-cache (lru-cache 32))

(defn user-profile-from-cache [siteid tuid]
  (get user-profile-cache (str siteid "-" tuid)))

(defn get-user-profile [http-client metrics-registry siteid tuid]
  (if-let [u (user-profile-from-cache siteid tuid)]
    u
    (let [user (read-user-profile http-client metrics-registry siteid tuid)]
      (when user
        (.put user-profile-cache (str siteid "-" tuid) user)
        user))))
