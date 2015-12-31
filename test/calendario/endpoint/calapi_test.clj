(ns calendario.endpoint.calapi-test
  (:require [com.stuartsierra.component :as component]
            [clojure.test :refer :all]
            [kerodon.core :refer :all]
            [kerodon.test :refer :all]
            [ring.util.io :refer [string-input-stream]]
            [ring.middleware.json :refer :all]
            [ring.mock.request :as mock]
            [cheshire.core :refer [parse-string generate-string]]
            [calendario.endpoint.calapi :as calapi]
            [metrics.core :refer [new-registry]]))

(def handler
  (calapi/calapi-endpoint { :metrics {:registry (new-registry)}}))

(deftest isworking-test
  (testing "isworking exists"
    (let [s (session handler)]
      (-> s
          (visit "/isworking")
          (has (status? 200) "page exists")))))

;curl -v -k -H "Content-Type: application/json" -X POST -d '{"expuserid": 600000, "email": "kurt@vonnegut.com", "tpid": 1, "eapid": 0, "tuid": 550000, "siteid": 1}' 'http://localhost:3000/api/user'

(deftest calendar-for-user-test
  (testing "do a get for a user's calendar, receive the url in json."
    (with-redefs [calendario.component.calendar-service/get-calendar-url-for-user
                  (fn [calendar-service siteid tuid] "/calendar/ical/jeffmad%40gmail.com/private-1234/trips.ics")]
      (let [response (handler (mock/request :get "/api/calendar/1/577015"))]
        (is (= (:status response) 200))
        (is (= (parse-string (:body response) true) {:url "/calendar/ical/jeffmad%40gmail.com/private-1234/trips.ics"}))
        (is (= (get-in response [:headers "Content-Type"])
               "application/json; charset=utf-8")))))
  (testing "verify 503 returned when error happens"
    (with-redefs [calendario.component.calendar-service/get-calendar-url-for-user
                  (fn [calendar-service siteid tuid] (throw (ex-info "some msg" {:cause :service-unavailable :error "something bad happened"})))]
      (let [response (handler (mock/request :get "/api/calendar/1/577015"))]
        (is (= (:status response) 503))
        (is (= (parse-string (:body response) true) {:error true :error-message "something bad happened" }))
        (is (= (get-in response [:headers "Content-Type"])
               "application/json; charset=utf-8"))))))

(deftest create-user-test
  (testing "making a new user admin"
    (with-redefs [calendario.component.calendar-service/make-user
                  (fn [calendar-service body] {:expuser {:iduser 1, :expuserid 12345, :email "jeffmad@gmail.com", :createdate #inst "2015-10-18T05:29:23.504000000-00:00"}, :siteusers [{:iduser 1, :idsiteuser 1, :tuid 577015, :tpid 1, :eapid 0, :siteid 1, :calid #uuid "6eb7f25d-4bbf-4753-aae2-72635fcd959b", :locale nil, :createdate #inst "2015-10-18T05:31:17.532000000-00:00"}]})
                  calendario.component.calendar-service/get-calendar-url-for-user
                  (fn [calendar-service siteid tuid] "/calendar/ical/jeffmad%40gmail.com/private-1234/trips.ics")]
      (let [response (handler (mock/request :post "/api/user" "{\"expuserid\": 600000, \"email\": \"kurt@vonnegut.com\", \"tpid\": 1, \"eapid\": 0, \"tuid\": 550000, \"siteid\": 1}"))]
        (is (= (:status response) 201))
        (is (= (get-in response [:headers "Location"])
               "/calendar/ical/jeffmad%40gmail.com/private-1234/trips.ics")))))
  (testing "verify 400 returned when malformed request received"
    (with-redefs [calendario.component.calendar-service/make-user
                  (fn [calendar-service body] (throw (ex-info "some msg" {:cause :malformed-request :error "something bad happened"})))
                  calendario.component.calendar-service/get-calendar-url-for-user
                  (fn [calendar-service siteid tuid] "/calendar/ical/jeffmad%40gmail.com/private-1234/trips.ics")]
      (let [response (handler (mock/request :post "/api/user" "{\"expuserid\": 600000, \"email\": \"kurt@vonnegut.com\", \"tpid\": 1, \"eapid\": 0, \"tuid\": 550000, \"siteid\": 1}"))]
        (is (= (:status response) 400))
        (is (= (parse-string (:body response) true) {:error true :error-message "something bad happened" }))
        (is (= (get-in response [:headers "Content-Type"])
               "application/json; charset=utf-8"))))))

(deftest reset-calendar-test
  (testing "reset the uuid of the calendar for a user"
    (with-redefs [calendario.component.calendar-service/reset-calendar-for-user
                  (fn [calendar-service siteid tuid uuid] nil)
                  calendario.component.calendar-service/get-calendar-url-for-user
                  (fn [calendar-service siteid tuid] "/calendar/ical/jeffmad%40gmail.com/private-1234/trips.ics")]
      (let [response (handler (mock/request :put "/api/reset-cal/1/577105"))]
        (is (= (:status response) 200))
        (is (= (:body response)
               (generate-string {:url "/calendar/ical/jeffmad%40gmail.com/private-1234/trips.ics" }))))))
  (testing "verify 503 returned when server error"
    (with-redefs [calendario.component.calendar-service/reset-calendar-for-user
                  (fn [calendar-service siteid tuid uuid] (throw (ex-info "something bad happened" {:cause :service-unavailable :error "something bad happened"})))
                  calendario.component.calendar-service/get-calendar-url-for-user
                  (fn [calendar-service siteid tuid] "/calendar/ical/jeffmad%40gmail.com/private-1234/trips.ics")]
      (let [response (handler (mock/request :put "/api/reset-cal/1/577015"))]
        (is (= (:status response) 503))
        (is (= (parse-string (:body response) true) {:error true :error-message "something bad happened" }))
        (is (= (get-in response [:headers "Content-Type"])
               "application/json; charset=utf-8"))))))

(deftest retrieve-calendar-test
  (testing "retrieve the user's calendar"
    (with-redefs [calendario.component.calendar-service/calendar-for
                  (fn [calendar-service email token] "BEGIN:VCALENDAR\nEND:VCALENDAR")]
      (let [response (handler (mock/request :get "/calendar/ical/jeffmad%40gmail.com/private-1234/trips.ics"))]
        (is (= (:status response) 200))
        (is (= (:body response)
               "BEGIN:VCALENDAR\nEND:VCALENDAR"))
        (is (= (get-in response [:headers "Content-Type"])
               "text/calendar; charset=utf-8")))))
  (testing "verify 503 returned when server error"
    (with-redefs [calendario.component.calendar-service/calendar-for
                  (fn [calendar-service email token] (throw (ex-info "something bad happened" {:cause :service-unavailable :error "something bad happened"})))]
      (let [response (handler (mock/request :get "/calendar/ical/jeffmad%40gmail.com/private-1234/trips.ics"))]
        (is (= (:status response) 503))
        (is (= (parse-string (:body response) true) {:error true :error-message "something bad happened" }))
        (is (= (get-in response [:headers "Content-Type"])
               "application/json; charset=utf-8"))))))
