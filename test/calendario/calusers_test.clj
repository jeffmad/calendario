(ns calendario.calusers-test
  (:require [clojure.test :refer :all]
            [calendario.calusers :refer :all]))

(deftest calendar-accessed
  (testing "checks for presence of a row in the table within n hours"
    (with-redefs [calendar-accessed-recently (fn [param-map con-map] '({:count 1}))]
      (is (= true (calendar-was-recently-accessed? {:db "yes"} 1 1))))
    (with-redefs [calendar-accessed-recently (fn [p c] '({:count 0}))]
      (is (= false (calendar-was-recently-accessed? {:db "yes"} 1 1))))))

(deftest record-calendar-access
  (testing "verify insert db record"
    (with-redefs [record-calendar-access<! (fn [p c] nil)]
      (is (= nil (record-calendar-access-by-user! {} 1 (java.time.Instant/now)))))))

(deftest users-that-need-calendar-rebuild
  (testing "get a list of users who need their cal rebuilt"
    (let [u '({:siteid 1 :tuid 333 :idsiteuser 10} {:siteid 1 :tuid 444 :idsiteuser 11})]
      (with-redefs [find-active-users-with-expiring-calendars (fn [p c] u)]
        (is (= (vec u) (users-need-fresh-calendars {:db "yes"})))))))

(deftest find-expuser-by-siteid-tuid-test
  (testing "find expuser"
    (let [u '({:iduser 1
               :expuserid 12345
               :email "jeffmad@gmail.com"
               :createdate #inst "2015-10-18T05:29:23.504000000-00:00"})]
      (with-redefs [expuser-by-siteid-tuid (fn [p c] u)]
        (is (= (first u) (find-expuser-by-siteid-tuid {:db "yes"} 1 577015)))))))

(deftest find-siteusers-by-iduser-test
  (testing "finding site users"
    (let [u '({:iduser 1, :idsiteuser 1, :tuid 577015, :tpid 1, :eapid 0, :siteid 1, :calid #uuid "6eb7f25d-4bbf-4753-aae2-72635fcd959b", :locale nil, :createdate #inst "2015-10-18T05:31:17.532000000-00:00"})]
      (with-redefs [siteusers-by-iduser (fn [p c] u)]
        (is (= (vec u) (find-siteusers-by-iduser {:db "yes"} 1)))))))

(deftest find-siteuser-test
  (testing "finding a siteuser"
    (let [u '({:iduser 1, :idsiteuser 1, :tuid 577015, :tpid 1, :eapid 0, :siteid 1, :calid #uuid "6eb7f25d-4bbf-4753-aae2-72635fcd959b", :locale nil})]
      (with-redefs [siteuser-by-siteid-tuid (fn [p c] u)]
        (is (= (first u) (find-siteuser {:db "yes"} 1 577015)))))))

(deftest user-lookup-test
  (testing "verifying user exists"
    (let [u '({:email "jeffmad@gmail.com", :eapid 0, :locale nil, :iduser 1, :siteid 1, :expuserid 12345, :tuid 577015, :calid #uuid "6eb7f25d-4bbf-4753-aae2-72635fcd959b", :tpid 1, :idsiteuser 1})]
      (with-redefs [check-user-exists (fn [p c] u)]
        (is (= (first u) (user-lookup {:db "yes"} "jeffmad@gmail.com" "6eb7f25d-4bbf-4753-aae2-72635fcd959b" )))))))

(deftest create-expuser-test
  (testing "adding an expuser to db"
    (let [u {:iduser 7, :expuserid 4444, :email "barry@whitehouse.gov", :createdate #inst "2015-11-13T07:40:37.013000000-00:00"}]
      (with-redefs [add-expuser<! (fn [p c] u)]
        (is (= u (create-exp-user! {:db "yes"} 4444 "barry@whitehouse.gov" (java.time.Instant/now))))))))

(deftest create-site-user-test
  (testing "adding site user to db"
    (let [u {:idsiteuser 11, :iduser 7, :calid #uuid "6eb7f25d-4bbf-4753-aae2-72635fcd959b", :tpid 1, :eapid 0, :tuid 55555, :siteid 1, :createdate #inst "2015-11-13T07:47:30.002000000-00:00", :locale "en_US"}]
      (with-redefs [add-siteuser<! (fn [p c] u)]
        (is (= u (create-site-user! {:db "yes"} 7 "6eb7f25d-4bbf-4753-aae2-72635fcd959b" 1 0 55555 1 "en_US" (java.time.Instant/now))))))))

(deftest next-calendar-pk-test
  (with-redefs [next-calendar-id (fn [p c] '({:nextval 22}))]
    (is (= 22 (next-calendar-pk {:db "yes"})))))

; it seems this is best served with an integration test
(deftest add-calendar-test
  (testing "add a calendar record and calendarsusers"
    (with-redefs [add-calendar<! (fn [p c] nil)
                  associate-cal-to-user<! (fn [p c] nil)]
      (is (= nil (add-calendar {:db "yes"} 111 "B E" 7 (java.time.Instant/now)))))))

(deftest expired?-test
  (testing "method to check if calendar is expired"
    (is (= true (expired? (java.time.Instant/now) (.plusSeconds (java.time.Instant/now) (* 60 60 -24)))))
    (is (= false (expired? (.plusSeconds (java.time.Instant/now) (* 60 60 -24)) (java.time.Instant/now))))))

(deftest valid?-test
  (testing "method to check if calendar is still valid"
    (is (= true (valid? (.plusSeconds (java.time.Instant/now) (* 60 60 -24)) (java.time.Instant/now))))
    (is (= false (valid? (java.time.Instant/now) (.plusSeconds (java.time.Instant/now) (* 60 60 -24)))))))

(deftest record-access-test
  (testing "verify that a row is inserted to record that user accessed calendar"
    (with-redefs [calendar-accessed-recently (fn [p c] '({:count 1}))
                  record-calendar-access<! (fn [p c] nil)]
      (is (= nil (record-access-if-not-present {:db "yes"} 1 1 577015))))))

(deftest is-calendar-older-than-test
  (testing "is calendar older than"
    (with-redefs [latest-calendar-created-for-user (fn [p c] '({:createdate #inst "2015-11-13T16:59:58.147000000-00:00"}))]
      (is (= true (is-latest-calendar-older-than? {:db "yes"} 1 (java.time.Instant/now))))
      (is (= false (is-latest-calendar-older-than? {:db "yes"} 1 (.toInstant #inst "2014-01-01T16:59:58.147000000-00:00") ))))))

(deftest latest-calendar-for-user-test
  (testing "latest calendar for user"
    (let [u '({:email "jeffmad@gmail.com", :eapid 0, :locale nil, :iduser 1, :siteid 1, :expuserid 12345, :tuid 577015, :calid #uuid "6eb7f25d-4bbf-4753-aae2-72635fcd959b", :tpid 1, :idsiteuser 1})]
    (with-redefs [latest-calendar-text-for-user (fn [p c] '({:icaltext "BEGIN:VCALENDAR\nPRODID:-//Expedia\\, Inc. //Trip Calendar V0.1//EN\nVERSION:2.0\nMETHOD:PUBLISH\nCALSCALE:GREGORIAN\nEND:VCALENDAR\n"
                                                             :createdate #inst "2015-11-13T16:59:58.147000000-00:00"
                                                             :idsiteuser 1
                                                             :siteid 1
                                                             :tuid 577015}))
                  calendar-accessed-recently (fn [p c] '({:count 1}))
                  record-calendar-access<! (fn [p c] nil)
                  check-user-exists (fn [p c] u) ]
      (is (= "BEGIN:VCALENDAR\nPRODID:-//Expedia\\, Inc. //Trip Calendar V0.1//EN\nVERSION:2.0\nMETHOD:PUBLISH\nCALSCALE:GREGORIAN\nEND:VCALENDAR\n"
             (latest-calendar-for-user {:db "yes"} "a@b.com" "DEADBEEF" (.toInstant #inst "2015-11-10T16:59:58.147000000-00:00"))))
      (is (= :expired (latest-calendar-for-user {:db "yes"} "a@b.com" "DEADBEEF" (java.time.Instant/now))))))))

(deftest reset-private-calendar-test
  (testing "able to assign new uuid to user"
    (with-redefs [siteuser-by-siteid-tuid (fn [p c] {:iduser 1
                                                     :idsiteuser 1
                                                     :tuid 577015
                                                     :tpid 1
                                                     :eapid 0
                                                     :siteid 1
                                                     :calid #uuid "6eb7f25d-4bbf-4753-aae2-72635fcd959c"
                                                     :locale "en_US"})
                  reset-calendar! (fn [p c] 1)]
      (is (= 1 (reset-private-calendar! {:db "yes"} 1 577015 "6eb7f25d-4bbf-4753-aae2-72635fcd959d"))))
    (with-redefs [siteuser-by-siteid-tuid (fn [p c] nil)]
      (is (nil? (reset-private-calendar! {:db "yes"} 1 577015 "6eb7"))))))
