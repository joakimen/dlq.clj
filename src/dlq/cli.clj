(ns dlq.cli
  (:require [dlq.core :as dlq]))

#_{:clj-kondo/ignore [:redefined-var]}
(defn list
  "list dead-letter queues with messages (--all to include empty queues)"
  [m]
  (dlq/list-queues m))

(defn redrive
  "select one or more queues to redrive messages from (--all to redrive all) "
  [m]
  (dlq/redrive m))
