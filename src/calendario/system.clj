(ns calendario.system
  (:require [com.stuartsierra.component :as component]
            [duct.component.endpoint :refer [endpoint-component]]
            [duct.component.handler :refer [handler-component]]
            ;[duct.component.hikaricp :refer [hikaricp]]
            [duct.middleware.not-found :refer [wrap-not-found]]
            [meta-merge.core :refer [meta-merge]]
            [ring.component.jetty :refer [jetty-server]]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [calendario.endpoint.calapi :refer [calapi-endpoint]]
            [calendario.component.http-client :refer [http-client]]
            [calendario.component.calendar-service :refer [calendar-service]]
            [calendario.component.scheduler :refer [scheduler]]
            [calendario.component.metrics :refer [metrics]]
            [calendario.component.hikaricp :refer [hikaricp]]))

(def base-config
  {:app {:middleware [[wrap-not-found :not-found]
                      [wrap-defaults :defaults]]
         :not-found  "Resource Not Found"
         :defaults   (meta-merge api-defaults {})}})

(defn new-system [config]
  (let [config (meta-merge base-config config)]
    (-> (component/system-map
         :app  (handler-component (:app config))
         :http (jetty-server (:http config))
         :http-client (http-client (:http-client config))
         :db   (hikaricp (:db config))
         :scheduler (scheduler (:scheduler config))
         :metrics (metrics (:metrics config))
         :calapi (endpoint-component calapi-endpoint)
         :calendar-service (calendar-service (:calendar-service config))
         )
        (component/system-using
         {:db [:metrics]
          :http [:app]
          :app  [:calapi]
          :calapi [:calendar-service :metrics]
          :scheduler [:calendar-service]
          :calendar-service [:db :http-client :metrics]
          }))))
