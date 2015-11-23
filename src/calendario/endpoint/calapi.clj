(ns calendario.endpoint.calapi
  (:require [compojure.core :refer :all]
            [clojure.java.io :as io]
            [ring.middleware.json :refer [wrap-json-body wrap-json-response]]
            [ring.util.response :refer [response created header content-type]]
            [calendario.component.calendar-service :as cs]
            [clojure.tools.logging :refer [error warn debug]]))

;curl -v -k -H "Content-Type: application/json" -X POST -d '{"expuserid": 600000, "email": "kurt@vonnegut.com", "tpid": 1, "eapid": 0, "tuid": 550000, "siteid": 1}' 'http://localhost:3000/api/user'
;curl -v -k  -X PUT 'http://localhost:3000/api/reset-cal/1/550000'


#_(defn wrap-library-exception
  [handler]
  (fn [request]
    (try
      (handler request)
      (catch clojure.lang.ExceptionInfo e
        (let [cause (:cause (ex-data e))
              status-code (status-code-for cause)]
          {:status status-code :body (.getMessage e)})))))

(defn status-code-for
  [cause]
  (case cause
    :malformed-request 400
    :resource-exists 303
    :internal-server-error 500
    :service-unavailable 503
    400))

; email siteid / expuserid tpid eapid tuid / uuid now
(defn cal-mgmt-routes [calendar-service]
  (routes
   (GET  "/calendar/:siteid{[0-9]+}/:tuid{[0-9]+}" [siteid tuid]
         (try
           (response
            {:url (cs/get-calendar-url-for-user
                   calendar-service
                   (Integer/parseInt siteid)
                   (Integer/parseInt tuid))})
           (catch clojure.lang.ExceptionInfo e
             (error e (str "caught exception getting calendar url for siteid: "
                           siteid " tuid:" tuid))
             (let [cause (:cause (ex-data e))
                   status-code (status-code-for cause)]
               {:status status-code :body {:error true :error-message (:error (ex-data e))}}))))
   (POST "/user" request
         (try
           (if-let [user (cs/make-user calendar-service (:body request))]
             (created
              (cs/get-calendar-url-for-user
                calendar-service (:siteid user) (:tuid user))))
           (catch clojure.lang.ExceptionInfo e
             (error e (str "caught exception creating user: " (:body request)))
             (let [cause (:cause (ex-data e))
                   status-code (status-code-for cause)]
               {:status status-code :body {:error true :error-message (:error (ex-data e))}}))))

   (PUT  "/reset-cal/:siteid{[0-9]+}/:tuid{[0-9]+}" [siteid tuid]
         (try
           (let [s (Integer/parseInt siteid)
                 t (Integer/parseInt tuid)]
             (cs/reset-calendar-for-user calendar-service s t (java.util.UUID/randomUUID))
             (response {:url (cs/get-calendar-url-for-user calendar-service s t)}))
           (catch clojure.lang.ExceptionInfo e
             (error e (str "caught exception resetting calendar for siteid: "
                           siteid " tuid: " tuid))
             (let [cause (:cause (ex-data e))
                   status-code (status-code-for cause)]
               {:status status-code :body {:error true :error-message (.getMessage e)}})))
         )))

(defn calapi-endpoint [{calendar-service :calendar-service}]
  (routes
   (wrap-json-response (GET "/isworking" [] (response {:version 1.0})))
   (context "/api" []
            (wrap-json-response
             (wrap-json-body
              (cal-mgmt-routes calendar-service) {:keywords? true})))
   (GET "/calendar/ical/:email/:token/trips.ics" [email token]
        (try
          (-> (response (cs/calendar-for calendar-service email token))
              (content-type "text/calendar; charset=utf-8"))
          (catch clojure.lang.ExceptionInfo e
            (error e (str "caught exception serving calendar for email: "
                          email))
            (let [cause (:cause (ex-data e))
                  status-code (status-code-for cause)]
              {:status status-code :body {:error true :error-message (.getMessage e)}}))))))
