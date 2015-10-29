(ns calendario.endpoint.calapi
  (:require [compojure.core :refer :all]
            [clojure.java.io :as io]
            [ring.middleware.json :refer [wrap-json-body wrap-json-response]]
            [ring.util.response :refer [response created header content-type]]
            [calendario.component.calendar-service :as cs]))

;curl -v -k -H "Content-Type: application/json" -X POST -d '{"expuserid": 600000, "email": "kurt@vonnegut.com", "tpid": 1, "eapid": 0, "tuid": 550000, "siteid": 1}' 'http://localhost:3000/api/user'
;curl -v -k  -X PUT 'http://localhost:3000/api/reset-cal/1/550000'

; email siteid / expuserid tpid eapid tuid / uuid now
(defn cal-mgmt-routes [calendar-service]
  (routes
   (GET  "/calendar/:siteid/:tuid" [siteid tuid]
         (response { :url (cs/get-calendar-url-for-user calendar-service (Integer/parseInt siteid) (Integer/parseInt tuid))}))
   (POST "/user" request
         (if-let [user (cs/make-user calendar-service (:body request))]
           (created (cs/get-calendar-url-for-user calendar-service (:siteid user) (:tuid user)))
           { :status 500 :body {:error true :error-message "User not created. Error occurred."}}))
   (PUT  "/reset-cal/:siteid/:tuid" [siteid tuid]
         (if (cs/reset-calendar-for-user calendar-service (Integer/parseInt siteid) (Integer/parseInt tuid) (java.util.UUID/randomUUID))
           (response {:url (cs/get-calendar-url-for-user calendar-service siteid tuid)})
             { :status 500 :body {:error true :error-message "Calendar not reset. Error occurred."}}))))

(defn calapi-endpoint [{calendar-service :calendar-service}]
  (routes
   (wrap-json-response (GET "/isworking" [] (response {:version 1.0})))
   (context "/api" []
            (wrap-json-response
             (wrap-json-body
              (cal-mgmt-routes calendar-service) {:keywords? true})))
   (GET "/calendar/ical/:email/:token/trips.ics" [email token]
        (-> (response (cs/calendar-for calendar-service email token))
            (content-type "text/calendar; charset=utf-8")))))
