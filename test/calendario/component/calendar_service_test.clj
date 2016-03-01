(ns calendario.component.calendar-service-test
  (:require [clojure.test :refer :all]
            [calendario.component.calendar-service :refer :all]))

(deftest a-test
  (testing "FIXME, I fail."
    (is (= 1 1))))

; service creation
; get-calendar-url-for-user - returns a string but what if there is an error?  throw exception?
(deftest get-calendar-url-for-user-test
  (testing "happy existing user"
    (with-redefs [calendario.user-manager/user-lookup (fn [db siteid tuid] {:expuser {:iduser 1, :expuserid 12345, :email "jeffmad@gmail.com", :createdate #inst "2015-10-18T05:29:23.504000000-00:00"}, :siteusers [{:iduser 1, :idsiteuser 1, :tuid 577015, :tpid 1, :eapid 0, :siteid 1, :calid #uuid "6eb7f25d-4bbf-4753-aae2-72635fcd959b", :locale nil, :createdate #inst "2015-10-18T05:31:17.532000000-00:00"}]})]
      (is (= "/calendar/ical/12345/private-6eb7f25d-4bbf-4753-aae2-72635fcd959b/trips.ics" (get-calendar-url-for-user {:db "yes"} 1 577015))))
    )
  (testing "happy nonexisting user"
    (with-redefs [calendario.user-manager/create-user (fn [db http reg siteid tuid] {:expuser {:iduser 1, :expuserid 12345, :email "jeffmad@gmail.com", :createdate #inst "2015-10-18T05:29:23.504000000-00:00"}, :siteusers [{:iduser 1, :idsiteuser 1, :tuid 577015, :tpid 1, :eapid 0, :siteid 1, :calid #uuid "6eb7f25d-4bbf-4753-aae2-72635fcd959b", :locale nil, :createdate #inst "2015-10-18T05:31:17.532000000-00:00"}]})
                  calendario.user-manager/user-lookup (fn [db siteid tuid] nil)]
      (is (= "/calendar/ical/12345/private-6eb7f25d-4bbf-4753-aae2-72635fcd959b/trips.ics" (get-calendar-url-for-user {:db "yes"} 1 577015)))))
  (testing "test for problems with userservice and db"
    (is (= 1 1)))
  (testing "test for userservice cannot find user"
    (is (= 1 1))))
; schema validate?
; make-user - throw exception or return :error?
; reset-calendar-for-user - returns true false
(deftest reset-calendar-url-for-user-test
  (testing "happy existing user"
    (with-redefs [calendario.user-manager/user-lookup (fn [db siteid tuid] {:expuser {:iduser 1, :expuserid 12345, :email "jeffmad@gmail.com", :createdate #inst "2015-10-18T05:29:23.504000000-00:00"}, :siteusers [{:iduser 1, :idsiteuser 1, :tuid 577015, :tpid 1, :eapid 0, :siteid 1, :calid #uuid "6eb7f25d-4bbf-4753-aae2-72635fcd959b", :locale nil, :createdate #inst "2015-10-18T05:31:17.532000000-00:00"}]})
                  calendario.calusers/reset-private-calendar! (fn [db siteid tuid uuid] 1)]
      (is (= true (reset-calendar-for-user {:db "yes"} 1 577015 "123"))))
    )
  (testing "user lookup returns false"
    (with-redefs [calendario.user-manager/user-lookup (fn [db siteid tuid] nil)]
      (is (= nil (reset-calendar-for-user {:db "yes"} 1 577015 "123"))))
    )
  (testing "test for problems with db"
    (is (= 1 1)))
  )
; build-and-store-calendar-for-user - total side effect return nil
; build-and-store-latest-calendar - total side effect return nil
; time-n-hours-before - returns time
(deftest time-n-hours-before-test
  (testing "with positive input"
    (let [t (java.time.Instant/parse "2015-12-25T12:00:00Z")]
      (is (= "2015-12-25T07:00:00Z" (.toString (time-n-hours-before t 5)))))
    )
  (testing "with negative input"
    (let [t (java.time.Instant/parse "2015-12-25T12:00:00Z")]
      (is (= "2015-12-25T07:00:00Z" (.toString (time-n-hours-before t -5)))))))
; refresh-stale-calendars - total side effect
; build-or-get-cached-calendar - return ical text
; calendar-for - return cal or nil
