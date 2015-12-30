(ns calendario-service.endpoint.hello-test
  (:require [clojure.test :refer :all]
            [ring.mock.request :as mock]
            [calendario-service.endpoint.hello :as hello]))

(def handler
  (hello/hello-endpoint {:hello {:message "Hello test!"}}))

(deftest hello-test
  (testing "hello returns configured message"
    (let [response (handler (mock/request :get "/api/hello"))]
      (is (= (get-in response [:body :message]) "Hello test!"))))
  (testing "hello returns message with name"
    (let [response (handler (mock/request :get "/api/hello/clojure"))]
      (is (= (get-in response [:body :message]) "Hello clojure!")))))
