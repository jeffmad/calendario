(ns calendario.system
  (:require [com.stuartsierra.component :as component]
            [duct.component.endpoint :refer [endpoint-component]]
            [duct.component.handler :refer [handler-component]]
            [duct.component.hikaricp :refer [hikaricp]]
            [duct.middleware.not-found :refer [wrap-not-found]]
            [meta-merge.core :refer [meta-merge]]
            [ring.component.jetty :refer [jetty-server]]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [calendario.endpoint.calapi :refer [calapi-endpoint]]
            [calendario.component.http-client :refer [http-client]]))

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
         :calapi (endpoint-component calapi-endpoint)
         )
        (component/system-using
         {:http [:app]
          :app  [:calapi]
          :calapi [:db :http-client]
          }))))
