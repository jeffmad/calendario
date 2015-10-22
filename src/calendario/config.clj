(ns calendario.config
  (:require [environ.core :refer [env]]))

(def defaults
  ^:displace {:http {:port 3000}})

(def environ
  {:http {:port (some-> env :port Integer.)}
   :http-client {:timeout 10 :threads 6}
   :db   {:uri  (env :database-url)}
   :calusers {:utc true}})
