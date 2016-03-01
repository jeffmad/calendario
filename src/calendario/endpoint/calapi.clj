(ns calendario.endpoint.calapi
  (:require [compojure.core :refer :all]
            [clojure.java.io :as io]
            [ring.middleware.json :refer [wrap-json-body wrap-json-response]]
            [ring.util.response :refer [response status created header content-type]]
            [calendario.component.calendar-service :as cs]
            [cheshire.core :refer [generate-string]]
            [clojure.tools.logging :refer [error warn debug]]
            [metrics.ring.instrument :refer [instrument]]
            [metrics.counters :refer [inc!]]
            [metrics.counters :refer [counter]]))

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
(defn cal-mgmt-routes [calendar-service reg]
  (routes
   (GET  "/calendar/:siteid{[0-9]+}/:tuid{[0-9]+}" [siteid tuid]
         (try
           (let [r (response
                    {:url (cs/get-calendar-url-for-user
                           calendar-service
                           (Integer/parseInt siteid)
                           (Integer/parseInt tuid))})]
             (inc! (counter  reg ["calendario" "api" "calurl"]))
             r)
           (catch clojure.lang.ExceptionInfo e
             (error e (str "caught exception getting calendar url for siteid: "
                           siteid " tuid:" tuid))
             (let [cause (:cause (ex-data e))
                   status-code (status-code-for cause)]
               {:status status-code :body {:error true :error-message (str (.getMessage e) " data: " (:error (ex-data e)))}}))
           (catch Throwable e
             (error e (str "caught throwable getting calendar url for siteid: "
                           siteid " tuid:" tuid))
             {:status 503
              :body {:error true :error-message (str (.toString e) " message:" (.getMessage e))}})))

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
                 t (Integer/parseInt tuid)
                 _ (cs/reset-calendar-for-user calendar-service s t (java.util.UUID/randomUUID))
                 r (response {:url (cs/get-calendar-url-for-user calendar-service s t)})]
             (inc! (counter reg ["calendario" "api" "calreset"]))
             r)
           (catch clojure.lang.ExceptionInfo e
             (error e (str "caught exception resetting calendar for siteid: "
                           siteid " tuid: " tuid))
             (let [cause (:cause (ex-data e))
                   status-code (status-code-for cause)]
               {:status status-code :body {:error true :error-message (.getMessage e)}})))
         )))

(defn calapi-endpoint [{calendar-service :calendar-service {reg :registry} :metrics}]
  (instrument (routes
               (wrap-json-response (GET "/isworking" [] (response {:version 1.0})))
               (context "/api" []
                        (wrap-json-response
                         (wrap-json-body
                          (cal-mgmt-routes calendar-service reg) {:keywords? true})))
               (GET "/calendar/ical/:expuserid/:token/trips.ics" [expuserid token]
                    (try
                      (let [e (Integer/parseInt expuserid)
                            r (-> (response (cs/calendar-for
                                             calendar-service
                                             e
                                             token))
                                  (content-type "text/calendar; charset=utf-8"))]
                        (inc! (counter reg ["calendario" "api" "calaccess"]))
                        r)
                      (catch clojure.lang.ExceptionInfo e
                        (error e (str "caught exception serving calendar for expuserid: "
                                      expuserid))
                        (let [cause (:cause (ex-data e))
                              status-code (status-code-for cause)]
                          (-> (response (generate-string
                                         {:error true :error-message (.getMessage e)}))
                              (content-type "application/json; charset=utf-8")
                              (status status-code)))))))
              reg))
