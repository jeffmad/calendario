(ns calendario.endpoint.calapi-test
  (:require [com.stuartsierra.component :as component]
            [clojure.test :refer :all]
            [kerodon.core :refer :all]
            [kerodon.test :refer :all]
            [calendario.endpoint.calapi :as calapi]))

(def handler
  (calapi/calapi-endpoint {}))

(deftest smoke-test
  (testing "index page exists"
    (-> (session handler)
        (visit "/isworking")
        (has (status? 200) "page exists"))))
