(ns calendario.endpoint.calapi
  (:require [compojure.core :refer :all]
            [clojure.java.io :as io]
            [ring.middleware.json :refer [wrap-json-body wrap-json-response]]
            [ring.util.response :refer [response created header]]
            [calendario.calusers :refer [reset-private-calendar!]]))

(defn make-user [m]
  { :code (.toString(java.util.UUID/randomUUID)) :id (inc (:tuid m))})

; email siteid / expuserid tpid eapid tuid / uuid now
(defn cal-mgmt-routes [db]
  (routes
   (GET  "/calendar/:siteid/:tuid" [siteid tuid] (response { :status "OK"}))
   (POST "/user" request (created (make-user (:body request))))
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
