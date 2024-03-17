(ns sqs.cli
  (:require [sqs.core :as sqs]))

#_{:clj-kondo/ignore [:redefined-var]}
(defn list
  "list all queues and their messages
   
   supported flags
     --non-empty: exclude empty queues
     --dlq: only return dead-letter queues
     --jira: print results in a table-format that can be pasted into jira (gfm)
     --edn: print results in edn-format that can be processed by other programs (gfm)"
  [m]
  (sqs/print-queues m))

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn redrive
  "select one or more queues to redrive messages from 
  
   supported flags
     --all: redrive all dlqs"
  [m]
  (sqs/redrive m))

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn get-messages
  "receive all messages from a queue and print :Body"
  [m]
  (sqs/read-messages m))
