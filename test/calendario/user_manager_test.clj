(ns calendario.user-manager-test
  (:require [clojure.test :refer :all]
            [calendario.user-manager :refer :all]))



                                        ; add-siteuser!
                                        ; create-user
                                        ; user-lookup

                                        ; exceptions or error codes?
; {:expuser {:iduser 1, :expuserid 12345, :email "jeffmad@gmail.com", :createdate #inst "2015-10-18T05:29:23.504000000-00:00"}, :siteusers [{:iduser 1, :idsiteuser 1, :tuid 577015, :tpid 1, :eapid 0, :siteid 1, :calid #uuid "6eb7f25d-4bbf-4753-aae2-72635fcd959b", :locale nil, :createdate #inst "2015-10-18T05:31:17.532000000-00:00"}]}
(deftest create-user-test
  (testing "create a new user"
    (with-redefs [calendario.user-service/get-user-profile
                  (fn [http-client siteid tuid] {:email "jeffmad@gmail.com"
                                                 :locale "en_US"})
                  calendario.user-service/get-user-by-email
                  (fn [http-client email] {:expuserid "301078"
                                           :email "jmadynski@expedia.com"
                                           :tuidmappings [{:tpid "1"
                                                           :tuid "5363093"
                                                           :single-use "true"}
                                                          {:tpid "1"
                                                           :tuid "577015"
                                                           :single-use "false"}]})
                  calendario.calusers/create-exp-user!
                  (fn [db expuserid email now] {:iduser 1
                                                :expuserid 12345
                                                :email "jeffmad@gmail.com"
                                                :createdate #inst "2015-10-18T05:29:23.504000000-00:00"})
                  calendario.calusers/create-site-user!
                  (fn [db iduser uuid tpid eapid tuid siteid locale now] {:calid #uuid "6eb7f25d-4bbf-4753-aae2-72635fcd959b",
                                                                          :createdate #inst "2015-10-18T05:31:17.532-00:00",
                                                                          :eapid 0,
                                                                          :idsiteuser 1
                                                                          :iduser iduser
                                                                          :locale locale
                                                                          :siteid siteid
                                                                          :tpid tpid
                                                                          :tuid tuid})]
      (is (= {:expuser {:iduser 1
                        :expuserid 12345
                        :email "jeffmad@gmail.com"
                        :createdate #inst "2015-10-18T05:29:23.504000000-00:00"}
              :siteusers [{:iduser 1
                           :idsiteuser 1
                           :tuid 5363093
                           :tpid 1
                           :eapid 0
                           :siteid 1
                           :calid #uuid "6eb7f25d-4bbf-4753-aae2-72635fcd959b"
                           :locale "en_US"
                           :createdate #inst "2015-10-18T05:31:17.532000000-00:00"}
                          {:iduser 1
                           :idsiteuser 1
                           :tuid 577015
                           :tpid 1
                           :eapid 0
                           :siteid 1
                           :calid #uuid "6eb7f25d-4bbf-4753-aae2-72635fcd959b"
                           :locale "en_US"
                           :createdate #inst "2015-10-18T05:31:17.532000000-00:00"}]}
             (create-user {:db 1} {:http-client 1} 1 577015))))))

(deftest user-lookup-test
  (testing "user lookup test"
    (with-redefs [calendario.calusers/find-siteuser
                    (fn [db siteid tuid] {:iduser 1})
                  calendario.calusers/find-expuser-by-siteid-tuid
                  (fn [db siteid tuid] {:iduser 1,
                                        :expuserid 12345
                                        :email "jeffmad@gmail.com"
                                        :createdate #inst "2015-10-18T05:29:23.504000000-00:00"})
                  calendario.calusers/find-siteusers-by-iduser
                  (fn [db iduser] [{:iduser 1
                                    :idsiteuser 1
                                    :tuid 577015
                                    :tpid 1
                                    :eapid 0
                                    :siteid 1
                                    :calid #uuid "6eb7f25d-4bbf-4753-aae2-72635fcd959b"
                                    :locale "en_US"
                                    :createdate #inst "2015-10-18T05:31:17.532000000-00:00"}])]
      (is (= {:expuser {:iduser 1
                        :expuserid 12345
                        :email "jeffmad@gmail.com"
                        :createdate #inst "2015-10-18T05:29:23.504000000-00:00"}
              :siteusers [{:iduser 1
                           :idsiteuser 1
                           :tuid 577015
                           :tpid 1
                           :eapid 0
                           :siteid 1
                           :calid #uuid "6eb7f25d-4bbf-4753-aae2-72635fcd959b"
                           :locale "en_US"
                           :createdate #inst "2015-10-18T05:31:17.532000000-00:00"}]}
             (user-lookup {:db 1} 1 577015))))))
