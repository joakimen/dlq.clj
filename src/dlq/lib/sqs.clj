(ns dlq.lib.sqs
  (:require [com.grzm.awyeah.client.api :as aws]
            [clojure.walk :refer [keywordize-keys]]))

(defn client []
  (aws/client {:api :sqs}))

(defn list-queues [client]
  (:QueueUrls (aws/invoke client {:op :ListQueues})))

(defn get-queue-attributes [client queue-url]
  (-> (aws/invoke client {:op :GetQueueAttributes
                          :request {:QueueUrl queue-url
                                    :AttributeNames ["All"]}})
      :Attributes
      keywordize-keys))

(defn start-message-move-task [client source-queue-arn]
  (:TaskHandle (aws/invoke client {:op :StartMessageMoveTask
                                   :request {:SourceArn source-queue-arn}})))

(defn receive-messages [client queue-url]
  (let [messages (aws/invoke client {:op :ReceiveMessage
                                     :request {:QueueUrl queue-url
                                               :VisibilityTimeout 15
                                               :WaitTimeSeconds 1
                                               :MaxNumberOfMessages 10}})]
    (when-not (empty? messages)
      (:Messages messages))))

(comment
  (def sqs (client))
  (-> (aws/ops sqs) keys sort)
  (aws/doc sqs :ReceiveMessage)
  ;;
  )
