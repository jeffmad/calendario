(ns calendario.component.calendar-service-test
  (:require [clojure.test :refer :all]
            [calendario.component.calendar-service :refer :all]))

(deftest a-test
  (testing "FIXME, I fail."
    (is (= 1 1))))

; service creation
; get-calendar-url-for-user - returns a string but what if there is an error?  throw exception?
; schema validate?
; make-user - throw exception or return :error?
; reset-calendar-for-user - returns true false
; build-and-store-calendar-for-user - total side effect return nil
; build-and-store-latest-calendar - total side effect return nil
; time-n-hours-ago - returns time
; refresh-stale-calendars - total side effect
; build-or-get-cached-calendar - return ical text
; calendar-for - return cal or nil
