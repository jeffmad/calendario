(ns calendario.trip-fetcher
  (:require [clj-http.client :as client]
            [cheshire.core :refer :all]
            [clojure.tools.logging :refer [error warn debug]]
            [slingshot.slingshot :refer [try+ throw+]]))

(defn get-url [url {:keys [conn-timeout socket-timeout conn-mgr]}]
  (try+
   (client/get url {:throw-exceptions true
                    :conn-timeout conn-timeout
                    :socket-timeout socket-timeout
                    :connection-manager conn-mgr })
   (catch java.net.SocketTimeoutException ste
     (error ste (str "socket timeout reading " url)))
   (catch java.net.ConnectException ce
     (error ce (str "connect exception to url " url)))
   (catch [:status 400] {:keys [request-time headers body]}
     (error (str "400 for url " url " time: " request-time " headers:" headers)))
   (catch [:status 500] {:keys [request-time headers body]}
     (error (str "500 for url " url " time: " request-time " headers:" headers)))
   (catch [:status 503] {:keys [request-time headers body]}
     (error (str "503 for url " url " time: " request-time " headers:" headers)))
   (catch Object _
     (error (:throwable &throw-context) "unexpected error")
     #_(throw+))))

; https://wwwexpediacom.integration.sb.karmalab.net/api/users/577015/trips?filterBookingStatus=BOOKED&filterTimePeriod=RECENTLY_COMPLETED&filterTimePeriod=UPCOMING&filterTimePeriod=INPROGRESS&getCachedDetails=10
(defn get-trips-for-user [{:keys [trip-service-endpoint conn-timeout socket-timeout conn-mgr]} tuid site-id]
  (let [url (format (str trip-service-endpoint "/api/users/%s/trips?siteid=%s") tuid site-id)
        resp (get-url url {:conn-timeout conn-timeout
                              :socket-timeout socket-timeout
                              :connection-manager conn-mgr })]
    (if (= 200 (:status resp))
      (parse-string  (:body resp) true)
      (throw (ex-info (str  "could not get trips for user, status="
                            (:status resp) " tuid: " tuid
                            " siteid: " site-id " response:" resp )
                      {:cause :service-unavailable
                       :error "did not get status 200 retrieving trips" })))))

(defn get-booked-upcoming-trips [http-client tuid site-id]
  (let [trips (->> (get-trips-for-user http-client tuid site-id)
                   :responseData
                   (into [] (comp (filter #(= (:bookingStatus %) "BOOKED"))
                                  (filter #(not= (:timePeriod %) "COMPLETED"))
                                  (map #(% :tripNumber)))))
        _ (debug (str "for user tuid: " tuid
                      " siteid " site-id
                      " tripcount: " (count trips)))]
    trips))

;(def t [{:bookingStatus "BOOKED" :timePeriod "UPCOMING" :tripNumber 1}{:bookingStatus "CANCELLED" :timePeriod "UPCOMING" :tripNumber 2}{:bookingStatus "BOOKED" :timePeriod "COMPLETED" :tripNumber 3} ])
#_(defn t-booked-upcoming [trip-summaries]
  (into [] (comp (filter #(= (:bookingStatus %) "BOOKED")) (filter #(not= (:timePeriod %) "COMPLETED")) (map #(% :tripNumber))) trip-summaries))

(defn get-trip-for-user [{:keys [trip-service-endpoint
                                 conn-timeout
                                 socket-timeout
                                 conn-mgr]} tuid site-id itin-number]
  (let [url (format (str trip-service-endpoint
                         "/api/users/%s/trips/%s?siteid=%s&useCache=true")
                    tuid itin-number site-id)
        _ (debug (str "getting trip " itin-number
                      " for tuid " tuid
                      " siteid: " site-id))
        resp (get-url url {
                       :conn-timeout conn-timeout
                       :socket-timeout socket-timeout
                       :connection-manager conn-mgr })
        _ (debug (str "reading trip " itin-number " took " (:request-time resp)))]
    (if (and resp (= 200 (:status resp)))
      (parse-string (:body resp) true)
      (do (error (str "error retrieving trip "
                      url " "
                      (with-out-str (clojure.pprint/pprint resp))))
        nil))))

(defn get-json-trips
  "given a user with siteid tuid and a list of trip numbers, retrieve the trip
   json for each trip. return a list of trip jsons"
  [http-client tuid site-id trip-numbers]
  (let [trip-f (partial get-trip-for-user http-client tuid site-id)
        json-trips (remove nil?  (map trip-f trip-numbers))
        _ (debug "For user " tuid
                 " site: " site-id
                 " upcoming trips:" (count trip-numbers)
                 " successfully read: " (count json-trips))]
    json-trips))
