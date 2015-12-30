(ns calendario-service.system
  (:require [com.stuartsierra.component :as component]
            [duct.component.endpoint :refer [endpoint-component]]
            [duct.component.handler :refer [handler-component]]
            [duct.middleware.not-found :refer [wrap-not-found]]
            [meta-merge.core :refer [meta-merge]]
            [ring.component.jetty :refer [jetty-server]]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [ring.middleware.json :refer [wrap-json-response wrap-json-body]]
            [calendario-service.endpoint.isactive :refer [isactive-endpoint]]
            [calendario-service.endpoint.hello :refer [hello-endpoint]]))

(def base-config
  {:app {:middleware [[wrap-not-found :not-found]
                      [wrap-defaults :defaults]
                      [wrap-json-response]
                      [wrap-json-body {:keywords? true}]]
         :not-found  "Resource Not Found"
         :defaults   (meta-merge api-defaults {})}})

(defn new-system [config]
  (let [config (meta-merge base-config config)]
    (-> (component/system-map
         :app  (handler-component (:app config))
         :http (jetty-server (:http config))
         :isactive (endpoint-component isactive-endpoint)
         :hello-endpoint (endpoint-component hello-endpoint)
         :hello (:hello config))
        (component/system-using
         {:http [:app]
          :app  [:isactive :hello-endpoint]
          :hello-endpoint [:hello]}))))
