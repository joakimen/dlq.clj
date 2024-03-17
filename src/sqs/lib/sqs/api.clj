(ns sqs.lib.sqs.api
  (:require [com.grzm.awyeah.client.api :as aws]
            [sqs.lib.async :refer [jxmap]]))

(defn client []
  (aws/client {:api :sqs}))

(defn- invoke
  "standardize keys for status and body"
  [client op-map]
  (let [resp (aws/invoke client op-map)]
    (if (:cognitect.anomalies/category resp)
      {:status :error :body (or (get-in resp [:Error :Message])
                                (get-in resp [:ErrorResponse :Error :Message]))}
      {:status :ok :body resp})))

(defn list-queues [client]
  (invoke client {:op :ListQueues}))

(defn get-queue-attributes [client queue-url]
  (invoke client {:op :GetQueueAttributes
                  :request {:QueueUrl queue-url
                            :AttributeNames ["All"]}}))

(defn start-message-move-task [client source-queue-arn]
  (invoke client {:op :StartMessageMoveTask
                  :request {:SourceArn source-queue-arn}}))

(defn receive-messages [client queue-url]
  (invoke client {:op :ReceiveMessage
                  :request {:QueueUrl queue-url
                            :VisibilityTimeout 15
                            :WaitTimeSeconds 1
                            :MaxNumberOfMessages 10}}))

(comment
  (def sqs (client))
  (-> (aws/ops sqs) keys sort)

  (def queues (list-queues sqs))
  queues

  (def attres-jx (doall (jxmap #(get-queue-attributes sqs %) queues)))
  attres-jx

  (def queue-attrs (get-queue-attributes sqs (first queues)))
  (clojure.pprint/pprint queue-attrs)

  ;;
  )
