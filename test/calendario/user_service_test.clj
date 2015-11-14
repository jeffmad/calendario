(ns calendario.user-service-test
  (:require [clojure.test :refer :all]
            [calendario.user-service :refer :all]))


; get-user-profile - http-client siteid tuid
; get-user-by-email http-client email (list tuids for account)

; happy path returns profile, email lookup returns at least one account
; http exceptions
; what happens if user not found or error in xml response
