(ns calendario.endpoint.calapi
  (:require [compojure.core :refer :all]
            [clojure.java.io :as io]
            [ring.middleware.json :refer [wrap-json-body wrap-json-response]]
            [ring.util.response :refer [response created header content-type]]
            [calendario.calusers :refer [reset-private-calendar! create-exp-user! create-site-user! latest-calendar-for-user user-lookup add-calendar-for-user!]]
            [calendario.user-manager :as um]
            [calendario.trip-fetcher :as tf]
            [calendario.calendar :as c]))

(defn get-calendar-url-for-user [db http-client siteid tuid]
  (let [u (or (um/user-lookup db siteid tuid)
              (um/create-user db http-client siteid tuid))
        email (get-in u [:expuser :email])
        uuid (:calid (first (filter #(= tuid (:tuid %)) (:siteusers u))))]
    (str "/calendar/ical/" (java.net.URLEncoder/encode email) "/private-" uuid "/trips.ics")))

(defn make-user [db http-client m]
  (let [now (java.time.Instant/now)
        siteid (:siteid m)
        tuid (:tuid m)
        expuser (create-exp-user! db (:expuserid m) (:email m) now)
        siteuser (create-site-user! db (:iduser expuser) (java.util.UUID/randomUUID) (:tpid m) (:eapid m) tuid siteid now)]
    (if (and expuser siteuser)
      (created (get-calendar-url-for-user db http-client siteid tuid))
      { :status 500 :body {:error true :error-message "User not created. Error occurred."}})))

(defn reset-calendar-for-user [db http-client siteid tuid uuid]
  (if (and (um/user-lookup db siteid tuid)
           (= 1 (reset-private-calendar! db siteid tuid uuid)))
    (response {:url (get-calendar-url-for-user db http-client siteid tuid)})
    { :status 500 :body {:error true :error-message "Calendar not reset. Error occurred."}}))

(defn time-n-hours-ago [hours]
  (let [h (if (pos? hours) (-' hours) hours)]
    (.plusSeconds (java.time.Instant/now) (* 60 60 h))))

(defn build-and-store-latest-calendar [db email uuid]
  (let [user (user-lookup db email uuid)
        idsiteuser (:idsiteuser user)
        cal-text (->> (tf/get-booked-upcoming-trips (:tuid user) (:siteid user) nil)
                      (tf/get-json-trips (:tuid user) (:siteid user) nil)
                      c/calendar-from-json-trips)
        _ (add-calendar-for-user! db cal-text idsiteuser (java.time.Instant/now))]
    cal-text))

(defn build-or-get-cached-calendar [db email uuid expire-time]
  (let [latest (latest-calendar-for-user db email uuid expire-time)]
    (if (= :expired latest)
      (build-and-store-latest-calendar db email uuid)
      latest)))

(defn calendar-for [db email token]
  (let [uuid (second (re-matches #"^private-([a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12})" token))
        expire-time (time-n-hours-ago 24)
        ;cal-text "BEGIN:VCALENDAR\r\nPRODID:-//Acme\\, Inc. //Acme Calendar V0.1//EN\r\nVERSION:2.0\r\nMETHOD:PUBLISH\r\nCALSCALE:GREGORIAN\r\nEND:VCALENDAR\r\n"
        cal-text (build-or-get-cached-calendar db email uuid expire-time)]
    (-> (response cal-text)
        (content-type "text/calendar; charset=utf-8"))))

;curl -v -k -H "Content-Type: application/json" -X POST -d '{"expuserid": 600000, "email": "kurt@vonnegut.com", "tpid": 1, "eapid": 0, "tuid": 550000, "siteid": 1}' 'http://localhost:3000/api/user'
;curl -v -k  -X PUT 'http://localhost:3000/api/reset-cal/1/550000'

; email siteid / expuserid tpid eapid tuid / uuid now
(defn cal-mgmt-routes [db http-client]
  (routes
   (GET  "/calendar/:siteid/:tuid" [siteid tuid] (response { :url (get-calendar-url-for-user db http-client (Integer/parseInt siteid) (Integer/parseInt tuid))}))
   (POST "/user" request (make-user db http-client (:body request)))
   (PUT  "/reset-cal/:siteid/:tuid" [siteid tuid] (reset-calendar-for-user db http-client (Integer/parseInt siteid) (Integer/parseInt tuid) (java.util.UUID/randomUUID)))))

(defn calapi-endpoint [{{db :spec} :db http-client :http-client}]
  (routes
   (wrap-json-response (GET "/isworking" [] (response {:version 1.0})))
   (context "/api" []
            (wrap-json-response
             (wrap-json-body
              (cal-mgmt-routes db http-client) {:keywords? true})))
   (GET "/calendar/ical/:email/:token/trips.ics" [email token]
        (calendar-for db email token))))
