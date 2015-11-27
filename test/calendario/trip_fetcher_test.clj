(ns calendario.trip-fetcher-test
  (:require [clojure.test :refer :all]
            [calendario.trip-fetcher :refer :all]))

                                        ; get-trips-for-user
                                        ; get-booked-upcoming-trips
                                        ; get-trip-for-user
                                        ; get-json-trips
(deftest get-trips-for-user-test
  (testing "retrieve trips for user"
    (with-redefs [calendario.trip-fetcher/get-url
                  (fn [url opts] {:status 200 :body "{\"some\": 1, \"another\": 2}" })
                  (is (= { :some 1 :another 2}
                         (get-trips-for-user {:trip-service-endpoint "http://some"
                                              :conn-timeout 1
                                              :socket-timeout 1
                                              :conn-mgr 1} 577015 1)))]))
  (testing "retrieve trips but exception happens"
    (with-redefs [calendario.trip-fetcher/get-url
                  (fn [url opts] (throw (ex-info "boom" {:cause :service-unavailable
                                                         :error "did not get status 200 retrieving trips"})))
                  (is (thrown? clojure.lang.ExceptionInfo
                         (get-trips-for-user {:trip-service-endpoint "http://some"
                                              :conn-timeout 1
                                              :socket-timeout 1
                                              :conn-mgr 1} 577015 1)))])))
(deftest get-booked-upcoming-trips-test
  (testing "given a tuid, siteid, get trips, then filter to get only booked upcoming"
    (with-redefs [calendario.trip-fetcher/get-trips-for-user
                  (fn [http-client tuid siteid] {:responseData [{:tripNumber 1 :timePeriod "COMPLETED" :bookingStatus "BOOKED"}
                                                                {:tripNumber 2 :timePeriod "UPCOMING" :bookingStatus "BOOKED"}
                                                                {:tripNumber 3 :timePeriod "UPCOMING" :bookingStatus "CANCELLED"}
                                                                {:tripNumber 4 :timePeriod "UPCOMING" :bookingStatus "BOOKED"}]})]
      (is (= [2 4]
             (get-booked-upcoming-trips {:http-client 1} 577015 1))))))

(deftest get-trip-for-user-test
  (testing "get the trip for the user"
    (with-redefs [calendario.trip-fetcher/get-url
                  (fn [url opts] {:status 200 :body "{\"some\": 1, \"another\": 2}" })
                  (is (= { :some 1 :another 2}
                         (get-trip-for-user {:trip-service-endpoint "http://some"
                                              :conn-timeout 1
                                              :socket-timeout 1
                                             :conn-mgr 1} 577015 1)))]))
  (testing "retrieve trip but exception happens"
    (with-redefs [calendario.trip-fetcher/get-url
                  (fn [url opts] (throw (ex-info "boom" {:cause :service-unavailable
                                                         :error "did not get status 200 retrieving trip"})))
                  (is (thrown? clojure.lang.ExceptionInfo
                               (get-trip-for-user {:trip-service-endpoint "http://some"
                                                    :conn-timeout 1
                                                    :socket-timeout 1
                                                    :conn-mgr 1} 577015 1)))])))
(deftest get-json-trips-test
  (testing "get a list of trip numbers given a user"
    (with-redefs [calendario.trip-fetcher/get-trip-for-user
                  (fn [http-client
                       tuid
                       siteid
                       itin-number]
                    (if (even? itin-number)
                      {:tripNumber itin-number :a 100 :b "GOOD"}
                      nil))]
      (is (= [{:tripNumber 2 :a 100 :b "GOOD"}
              {:tripNumber 4 :a 100 :b "GOOD"}]
             (get-json-trips {:http-client 1} 577015 1 [1 2 3 4]))))))
