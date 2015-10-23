(ns calendario.trip-fetcher
  (:require [clj-http.client :as client]
            [cheshire.core :refer :all]
            [clojure.tools.logging :refer [log error warn debug]]))

(defn get-trips-for-user [tuid site-id cm]
  (let [url (format "http://wwwexpediacom.integration.sb.karmalab.net/api/users/%s/trips?siteid=%s" tuid site-id)
        resp (client/get url)]
    ;resp (client/get url {:connection-manager cm :debug true})
    (if (= 200 (:status resp))
      (parse-string  (:body resp) true)
      (throw (RuntimeException. (str  "could not get trips for user, status=" (:status resp) " tuid: " tuid " siteid: " site-id " response:" resp ))))))

(defn get-booked-upcoming-trips [tuid site-id cm]
  (->> (get-trips-for-user tuid site-id cm)
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

(defn get-trip-for-user [tuid site-id cm itin-number]
  (let [url (format "http://wwwexpediacom.integration.sb.karmalab.net/api/users/%s/trips/%s?siteid=%s" tuid itin-number site-id)
                                        ;resp (client/get url {:connection-manager cm :debug true})]
        resp (client/get url {:throw-exceptions false})]
    (if (= 200 (:status resp))
      (parse-string (:body resp) true)
      (do (error (str "error retrieving trip " url " " (with-out-str (clojure.pprint/pprint resp))))
        nil)
      #_(throw (RuntimeException. (str  "could not get trip for user, status=" (:status resp) " url was: " url  " response:"  resp ))))))
