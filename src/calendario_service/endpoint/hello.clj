(ns calendario-service.endpoint.hello
  (:require [compojure.core :refer :all]
            [clojure.tools.logging :as log]))

(defn hello-endpoint [config]
  (context "/api/hello" []
    (GET "/" []
      {:body    (let [body {:message (get-in config [:hello :message])}]
                  (log/debug (str "Response: " body))
                  body)
       :headers {"Content-Type" "application/json"}
       :status  200})
    (GET "/:name" [name]
      {:body    {:message (str "Hello " name "!")}
       :headers {"Content-Type" "application/json"}
       :status  200})))
