(ns calendario.endpoint.isactive
  (:import
    (com.expedia.www.platform.isactive.resources IsActiveResource BuildInfoResource)
    (com.expedia.www.platform.isactive.providers FileBasedActiveVersionProvider))
  (:require [compojure.core :refer :all]
            [clojure.java.io :as io]))

(defn isactive-endpoint [config]
  (let [build-info-resource (new BuildInfoResource (slurp (io/resource "buildInfo.json")))
        is-active-resource (new IsActiveResource build-info-resource (new FileBasedActiveVersionProvider))]
    (routes
      (GET "/buildInfo" []
        (let [build-info-response (.buildInfo build-info-resource)]
          {:status  (.getStatus build-info-response)
           :body    (.getEntity build-info-response)
           :headers {"Cache-Control" "no-cache,no-store,max-age=0"
                     "Content-Type"  "application/json"}}))
      (GET "/isActive" []
        (let [is-active-response (.isActive is-active-resource)]
          {:status  (.getStatus is-active-response)
           :body    (.getEntity is-active-response)
           :headers {"Cache-Control" "no-cache,no-store,max-age=0"
                     "Content-Type"  "text/plain"}})))))
