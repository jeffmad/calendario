(ns calendario.component.scheduler
  (:require [com.stuartsierra.component :as component]
            [overtone.at-at :refer [mk-pool stop-and-reset-pool! show-schedule]]
            [clojure.tools.logging :refer [log error warn debug]])
  (​:import​ [java.util.concurrent Executors]))

(defrecord Scheduler []
  component/Lifecycle
  (start [this] this)
  (stop [this] this))

(defn scheduler []
  (->Scheduler))

;(def p (at/mk-pool :cpu-count 1))
;(def j (at/every 1000 #(println (str  "Hi " (java.time.Instant/now))) p :fixed-delay true :initial-delay 5000))
;(at/show-schedule p)
;(at/stop j)

; stop, kill take job or id pool
;Returns a scheduled-fn which may be cancelled with cancel.
;(every 1000 #(println "I am cool!") my-pool :fixed-delay true :initial-delay 2000)
;(stop-and-reset-pool! my-pool :strategy :kill)

; interspaced
;(​import​ '[java.util.concurrent Executors])
;(​def​ processors (​.​availableProcessors (Runtime/getRuntime)))
;(​defonce​ executor (Executors/newFixedThreadPool processors))
;(​defn​ submit-task [^Runnable task]
;  (​.​submit executor task))
