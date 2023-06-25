(ns dlq.cli
  (:require [dlq.core :as dlq]))

(defn list-queues
  "list dead-letter queues with messages (--all to include empty queues)"
  [m]
  (dlq/list-queues m))

(defn start-redrive
  "select one or more queues to redrive messages from (--all to redrive all) "
  [m]
  (dlq/redrive m))

(defn read-messages
  "receive all messages from a queue and print :Body"
  [m]
  (dlq/read-messages m))
