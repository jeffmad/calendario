(ns calendario.trip-fetcher
  (:require [clj-http.client :as client]
            [cheshire.core :refer :all]
            [clojure.tools.logging :refer [log error warn debug]]))

(defn get-trips-for-user [http-client tuid site-id]
  (let [{:keys [trip-service-endpoint conn-timeout socket-timeout connection-manager]} http-client
        url (format (str trip-service-endpoint "/api/users/%s/trips?siteid=%s") tuid site-id)
        resp (client/get url {:conn-timeout conn-timeout
                              :socket-timeout socket-timeout
                              :connection-manager conn-mgr })]
    (if (= 200 (:status resp))
      (parse-string  (:body resp) true)
      (throw (RuntimeException. (str  "could not get trips for user, status=" (:status resp) " tuid: " tuid " siteid: " site-id " response:" resp ))))))

(defn get-booked-upcoming-trips [http-client tuid site-id]
  (->> (get-trips-for-user http-client tuid site-id)
      :responseData
      (into [] (comp (filter #(= (:bookingStatus %) "BOOKED")) (filter #(not= (:timePeriod %) "COMPLETED")) (map #(% :tripNumber))))))

;(def t [{:bookingStatus "BOOKED" :timePeriod "UPCOMING" :tripNumber 1}{:bookingStatus "CANCELLED" :timePeriod "UPCOMING" :tripNumber 2}{:bookingStatus "BOOKED" :timePeriod "COMPLETED" :tripNumber 3} ])
(defn t-booked-upcoming [trip-summaries]
  (into [] (comp (filter #(= (:bookingStatus %) "BOOKED")) (filter #(not= (:timePeriod %) "COMPLETED")) (map #(% :tripNumber))) trip-summaries))

#_(try+
 (client/get url)
 (catch [:status 400] {:keys [request-time headers body]}
   (log/warn "400" request-time headers))
 (catch [:status 503] {:keys [request-time headers body]}
   (log/warn "503" request-time headers)))
; java.net.SocketTimeoutException
(defn get-trip-for-user [http-client tuid site-id itin-number]
  (let [{:keys [trip-service-endpoint conn-timeout socket-timeout connection-manager]} http-client
        url (format (str trip-service-endpoint "/api/users/%s/trips/%s?siteid=%s") tuid itin-number site-id)
        resp (client/get url {:throw-exceptions false
                              :conn-timeout conn-timeout
                              :socket-timeout socket-timeout
                              :connection-manager conn-mgr })]
    (if (= 200 (:status resp))
      (parse-string (:body resp) true)
      (do (error (str "error retrieving trip " url " " (with-out-str (clojure.pprint/pprint resp))))
        nil)
      #_(throw (RuntimeException. (str  "could not get trip for user, status=" (:status resp) " url was: " url  " response:"  resp ))))))

(defn get-json-trips [http-client tuid site-id trip-numbers]
  (let [trip-f (partial get-trip-for-user http-client tuid site-id)
        json-trips (remove nil?  (map trip-f trip-numbers))
        _ (debug "For user " tuid " site: " site-id " upcoming trips:" (count trip-numbers) " successfully read: " (count json-trips))]
    json-trips))
