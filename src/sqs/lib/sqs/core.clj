(ns sqs.lib.sqs.core
  (:require [clojure.string :as str]
            [clojure.walk :refer [keywordize-keys]]
            [sqs.lib.async :refer [jxmap]]
            [sqs.lib.sqs.api :as api]))

(defn client []
  (api/client))

(defn queue-shortname
  "extract the name-part of a sqs queue url or arn"
  [arn-or-url]
  (-> arn-or-url (str/split #"/|:") last))

(defn dead-letter-queue? [queue-url]
  (some? (re-find #"-dlq$" queue-url)))

(defn- list-queues [client]
  (let [queue-resp (api/list-queues client)
        {:keys [status body]} queue-resp]
    (if (= :error status)
      (throw (ex-info (str "An error occurred while listing queues: " body) {:babashka/exit 1}))
      (:QueueUrls body))))

(defn get-queue-attributes [client queue-url]
  (let [attr-resp (api/get-queue-attributes client queue-url)
        {:keys [status body]} attr-resp
        attr-body (if (= :error status)
                    body
                    (let [{:keys [QueueArn ApproximateNumberOfMessages]} (-> body :Attributes keywordize-keys)]
                      {:arn QueueArn
                       :messages (Integer/parseInt ApproximateNumberOfMessages)}))]
    {:name (queue-shortname queue-url)
     :url queue-url
     :attributes {:status status
                  :body attr-body}}))

(defn get-queues-and-attributes
  [client]
  (let [queues (list-queues client)]
    (jxmap #(get-queue-attributes client %) queues)))

(defn start-message-move-task [client source-queue-arn]
  (let [redrive-resp (api/start-message-move-task client source-queue-arn)
        {:keys [status body]} redrive-resp]
    {:arn source-queue-arn
     :redrive-result {:status status
                      :body body}}))

;;;;;;;;;;;;;;;;;;;;;;; WIP
(defn receive-all-messages
  "read all messages from a queue"
  [sqs queue-url]
  (let [messages (atom [])
        received (atom [])]
    (while (reset! received (api/receive-messages sqs queue-url))
      (swap! messages concat @received))
    @messages))

(defn receive-messages [client queue-url]
  (let [messages (api/receive-messages client queue-url)]
    (when-not (empty? messages)
      (:Messages messages))))
;;;;;;;;;;;;;;;;;; WIP

(comment

  (def client (api/client))
  (do
    (def queues (list-queues client))
    queues)

  (do
    (def redrive-resp (api/start-message-move-task client  "arn:aws:sqs:eu-west-1:849138267389:europris-dev-product-ingress2-dlq"))
    redrive-resp)

  :rcf)
