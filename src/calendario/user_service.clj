(ns calendario.user-service
  (:require [clj-http.client :as client]
            [clojure.tools.logging :refer [log error warn debug]]
            [clojure.data.xml :as xml]
            [clojure.zip :as zip]
            [clojure.data.zip.xml :as zx]))

(def exp-account-template "<?xml version=\"1.0\" encoding=\"utf-8\"?><usr:listTUIDsForExpAccountRequest xmlns:usr=\"urn:com:expedia:www:services:user:messages:v3\" siteID=\"1\"><usr:expUser emailAddress=\"%s\" /><usr:messageInfo enableTraceLog=\"false\" clientName=\"localhost\" transactionGUID=\"a2192179-d5b7-4234-918c-8f662aaaf545\"/></usr:listTUIDsForExpAccountRequest>")

;<?xml version="1.0" encoding="utf-8" standalone="yes"?><listTUIDsForExpAccountResponse success="true" xmlns="urn:com:expedia:www:services:user:messages:v3"><expUser id="301078" emailAddress="jmadynski@expedia.com"/><expUserTUIDMapping tpid="1" tuid="5363093" singleUse="true" updateDate="2014-04-25T09:55:00.000-07:00"/><expUserTUIDMapping tpid="1" tuid="577015" singleUse="false" updateDate="2014-05-30T22:26:00.000-07:00"/><authRealmID>1</authRealmID></listTUIDsForExpAccountResponse>


(defn tuid-mapping [t]
  {:tpid (zx/xml1-> t (zx/attr :tpid))
   :tuid (zx/xml1-> t (zx/attr :tuid))
   :single-use (zx/xml1-> t (zx/attr :singleUse))})

(defn accounts [r]
  (let [x (xml/parse (java.io.StringReader. r))
        z (zip/xml-zip x)]
    (if (= "true" (zx/xml1-> z (zx/attr :success)))
      { :expuserid (zx/xml1-> z :expUser (zx/attr :id))
       :email (zx/xml1-> z :expUser (zx/attr :emailAddress))
       :tuidmappings (mapv tuid-mapping (zx/xml-> z :expUserTUIDMapping))}
      (error (str "profile response not successful " r)))))

;{:expuserid "301078", :email "jmadynski@expedia.com", :tuidmappings [{:tpid "1", :tuid "5363093", :single-use "true"} {:tpid "1", :tuid "577015", :single-use "false"}]}
; "https://userservicev3.integration.karmalab.net:56783"
(defn get-user-by-email [http-client email]
  (let [{:keys [user-service-endpoint conn-timeout socket-timeout conn-mgr]} http-client
        url (str user-service-endpoint "/exp-account/tuids")
        resp (client/post url {:body (format exp-account-template email)
                               :content-type :xml
                               :accept :xml
                               :insecure? true
                               :conn-timeout conn-timeout
                               :socket-timeout socket-timeout
                               :connection-manager conn-mgr })]
    (if (= 200 (:status resp))
      (accounts (:body resp))
      nil)))

(defn profile [r]
  (let [x (xml/parse (java.io.StringReader. r))
        z (zip/xml-zip x)]
    (if (= "true" (zx/xml1-> z (zx/attr :success)))
      { :email (zx/xml1-> z :user (zx/attr :emailAddress))
       :locale (zx/xml1-> z :user (zx/attr :locale))
       :first (zx/xml1-> z :user :personalName (zx/attr :first))
       :last (zx/xml1-> z :user :personalName (zx/attr :last))
       :country-code (zx/xml1-> z :user :preferredPhone (zx/attr :countryCode))
       :phone (zx/xml1-> z :user :preferredPhone (zx/attr :phoneNumber))}
      (error (str "profile response not successful " r)))))

(def exp-profile-template "<?xml version=\"1.0\" encoding=\"utf-8\"?><usr:getUserProfileRequest xmlns:usr=\"urn:com:expedia:www:services:user:messages:v3\" siteID=\"%s\"><usr:user actAsTuid=\"%s\" loggedInTuid=\"%s\"/><usr:messageInfo enableTraceLog=\"false\" clientName=\"localhost\" transactionGUID=\"a2192179-d5b7-4234-918c-8f662aaaf545\"/></usr:getUserProfileRequest>")
;"https://userservicev3.integration.karmalab.net:56783"
(defn get-user-profile [http-client site-id tuid]
  (let [{:keys [user-service-endpoint conn-timeout socket-timeout conn-mgr]} http-client
        url (str user-service-endpoint "/profile/get")
        resp (client/post url {:body (format exp-profile-template site-id tuid tuid)
                               :content-type :xml
                               :accept :xml
                               :insecure? true
                               :conn-timeout conn-timeout
                               :socket-timeout socket-timeout
                               :connection-manager conn-mgr })]
    (if (= 200 (:status resp))
      (profile (:body resp))
      nil)))

; curl -v -k -H "Content-Type: text/xml" -X POST -d '<?xml version="1.0" encoding="utf-8"?><usr:getUserProfileRequest xmlns:usr="urn:com:expedia:www:services:user:messages:v3"><usr:user actAsTuid="577015" loggedInTuid="577015"/><usr:pointOfSale tpid="1" eapid="0"/><usr:messageInfo enableTraceLog="false" clientName="localhost" transactionGUID="a2192179-d5b7-4234-918c-8f662aaaf545"/></usr:getUserProfileRequest>' 'https://userservicev3.integration.karmalab.net:56783/profile/get'
;<?xml version="1.0" encoding="utf-8" standalone="yes"?><getUserProfileResponse success="true" xmlns="urn:com:expedia:www:services:user:messages:v3"><user passengerType="2" emailAddress="jmadynski@expedia.com" loginName="577015_EmailSignIn" hasGroups="false" locale="en_US"><personalName titleID="0" first="Elmer" middle="" last="Fudd"/><preferredPhone countryCode="1" phoneNumber="(415) 5050490"/><paymentInstrument paymentInstrumentID="75F85DBA-9274-4FC3-B7D6-E24BE4C5580C"><description>myvisa</description></paymentInstrument><paymentInstrument paymentInstrumentID="35DA0E90-A3DC-4C48-93AD-415B1B1D59E0"><description>otre Visa</description></paymentInstrument></user></getUserProfileResponse>
;curl -v -k -H "Content-Type: text/xml" -X POST -d '<?xml version="1.0" encoding="utf-8"?><usr:listTUIDsForExpAccountRequest xmlns:usr="urn:com:expedia:www:services:user:messages:v3"><usr:expUser emailAddress="jmadynski@expedia.com"/><usr:pointOfSale tpid="1" eapid="0"/><usr:messageInfo enableTraceLog="false" clientName="localhost" transactionGUID="a2192179-d5b7-4234-918c-8f662aaaf545"/></usr:listTUIDsForExpAccountRequest>' 'https://userservicev3.integration.karmalab.net:56783/exp-account/tuids'
;<?xml version="1.0" encoding="utf-8" standalone="yes"?><listTUIDsForExpAccountResponse success="true" xmlns="urn:com:expedia:www:services:user:messages:v3"><expUser id="301078" emailAddress="jmadynski@expedia.com"/><expUserTUIDMapping tpid="1" tuid="5363093" singleUse="true" updateDate="2014-04-25T09:55:00.000-07:00"/><expUserTUIDMapping tpid="1" tuid="577015" singleUse="false" updateDate="2014-05-30T22:26:00.000-07:00"/><authRealmID>1</authRealmID></listTUIDsForExpAccountResponse>



;<?xml version="1.0" encoding="utf-8" standalone="yes"?><getUserProfileResponse success="false" xmlns="urn:com:expedia:www:services:user:messages:v3"><errorMessage errorCode="IllegalArguments">Invalid traveler (LoggedInTUID: 577015 or ActAsTUID: 577015).</errorMessage></getUserProfileResponse>
;<?xml version="1.0" encoding="utf-8" standalone="yes"?><getUserProfileResponse success="true" xmlns="urn:com:expedia:www:services:user:messages:v3"><user passengerType="2" emailAddress="jmadynski@expedia.com" loginName="577015_EmailSignIn" hasGroups="false" locale="en_US"><personalName titleID="0" first="Elmer" middle="" last="Fudd"/><preferredPhone countryCode="1" phoneNumber="(415) 5050490"/><paymentInstrument paymentInstrumentID="75F85DBA-9274-4FC3-B7D6-E24BE4C5580C"><description>myvisa</description></paymentInstrument><paymentInstrument paymentInstrumentID="35DA0E90-A3DC-4C48-93AD-415B1B1D59E0"><description>otre Visa</description></paymentInstrument></user></getUserProfileResponse>
