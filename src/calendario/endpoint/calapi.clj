(ns calendario.endpoint.calapi
  (:require [compojure.core :refer :all]
            [clojure.java.io :as io]
            [ring.middleware.json :refer [wrap-json-body wrap-json-response]]
            [ring.util.response :refer [response created header]]
            [calendario.calusers :refer [reset-private-calendar!]]))


; curl -v -k -H "Content-Type: text/xml" -X POST -d '<?xml version="1.0" encoding="utf-8"?><usr:getUserProfileRequest xmlns:usr="urn:com:expedia:www:services:user:messages:v3"><usr:user actAsTuid="577015" loggedInTuid="577015"/><usr:pointOfSale tpid="1" eapid="0"/><usr:messageInfo enableTraceLog="false" clientName="localhost" transactionGUID="a2192179-d5b7-4234-918c-8f662aaaf545"/></usr:getUserProfileRequest>' 'https://userservicev3.integration.karmalab.net:56783/profile/get'
;<?xml version="1.0" encoding="utf-8" standalone="yes"?><getUserProfileResponse success="true" xmlns="urn:com:expedia:www:services:user:messages:v3"><user passengerType="2" emailAddress="jmadynski@expedia.com" loginName="577015_EmailSignIn" hasGroups="false" locale="en_US"><personalName titleID="0" first="Elmer" middle="" last="Fudd"/><preferredPhone countryCode="1" phoneNumber="(415) 5050490"/><paymentInstrument paymentInstrumentID="75F85DBA-9274-4FC3-B7D6-E24BE4C5580C"><description>myvisa</description></paymentInstrument><paymentInstrument paymentInstrumentID="35DA0E90-A3DC-4C48-93AD-415B1B1D59E0"><description>otre Visa</description></paymentInstrument></user></getUserProfileResponse>
;curl -v -k -H "Content-Type: text/xml" -X POST -d '<?xml version="1.0" encoding="utf-8"?><usr:listTUIDsForExpAccountRequest xmlns:usr="urn:com:expedia:www:services:user:messages:v3"><usr:expUser emailAddress="jmadynski@expedia.com"/><usr:pointOfSale tpid="1" eapid="0"/><usr:messageInfo enableTraceLog="false" clientName="localhost" transactionGUID="a2192179-d5b7-4234-918c-8f662aaaf545"/></usr:listTUIDsForExpAccountRequest>' 'https://userservicev3.integration.karmalab.net:56783/exp-account/tuids'
;<?xml version="1.0" encoding="utf-8" standalone="yes"?><listTUIDsForExpAccountResponse success="true" xmlns="urn:com:expedia:www:services:user:messages:v3"><expUser id="301078" emailAddress="jmadynski@expedia.com"/><expUserTUIDMapping tpid="1" tuid="5363093" singleUse="true" updateDate="2014-04-25T09:55:00.000-07:00"/><expUserTUIDMapping tpid="1" tuid="577015" singleUse="false" updateDate="2014-05-30T22:26:00.000-07:00"/><authRealmID>1</authRealmID></listTUIDsForExpAccountResponse>

(defn make-user [m]
  { :code (.toString(java.util.UUID/randomUUID)) :id (inc (:tuid m))})

; email siteid / expuserid tpid eapid tuid / uuid now
(defn cal-mgmt-routes [db]
  (routes
   (GET  "/calendar/:siteid/:tuid" [siteid tuid] (response { :status "OK"}))
   (POST "/user" request (created  "abc" (make-user (:body request))))
   (PUT  "/reset-cal/:siteid/:tuid" [siteid tuid] (response { :status "COOL"}))))

(defn calapi-endpoint [{{db :spec} :db http-client :http-client}]
  (routes
   (wrap-json-response (GET "/isworking" [] (response {:version 1.0})))
   (context "/api" []
            (wrap-json-response
             (wrap-json-body
              (cal-mgmt-routes db) {:keywords? true})))
   (GET "/calendar/ical/:email/:token/trips.ics" [email token]
        (header (response "BEGIN:VCALENDAR\r\nPRODID:-//Acme\\, Inc. //Acme Calendar V0.1//EN\r\nVERSION:2.0\r\nMETHOD:PUBLISH\r\nCALSCALE:GREGORIAN\r\nEND:VCALENDAR\r\n") "Content-Type" "text/calendar; charset=utf-8"))))
