(ns calendario.endpoint.calapi
  (:require [compojure.core :refer :all]
            [clojure.java.io :as io]
            [ring.middleware.json :refer [wrap-json-body wrap-json-response]]
            [ring.util.response :refer [response header]]
            [calendario.calusers :refer [reset-private-calendar!]]))

(defn ical-routes [db http-client]
  (routes
   ))

(defn calapi-endpoint [{{db :spec} :db http-client :http-client}]
  (routes
   (wrap-json-response (wrap-json-body (GET "/isworking" [] (response {:version 1.0}))))
   (GET "calendar/ical/:email/:token/trips.ics" [email token]
        (header (response { :version 1.0 :email email :token token}) "Content-Type" "text/calendar; charset=utf-8"))))
