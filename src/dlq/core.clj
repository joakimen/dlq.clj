(ns dlq.core
  (:require [clojure.string :as str]
            [dlq.lib.sqs :as sqs]
            [doric.core :as doric]
            [fzf.core :refer [fzf]]
            [cheshire.core :as json]))

(defn- queue-shortname
  "extract the name-part of a sqs queue url or arn"
  [arn-or-url]
  (-> arn-or-url (str/split #"/|:") last))

(defn- get-queues-and-attributes
  "list all dead-letter queues with their attributes"
  [client]
  (let [queues (->> (sqs/list-queues client)
                    (filter #(re-find #"-dlq$" %)))
        queue-attrs (->> queues
                         (pmap #(merge {:QueueUrl %}
                                       (sqs/get-queue-attributes client %)))
                         (doall))]
    (->> queue-attrs
         (map (fn [{:keys [QueueArn QueueUrl ApproximateNumberOfMessages]}]
                {:name (queue-shortname QueueUrl)
                 :arn QueueArn
                 :url QueueUrl
                 :messages (Integer/parseInt ApproximateNumberOfMessages)})))))

(defn- filter-with-fzf
  "filter a list of queues interactively with fzf

   fzf doesn't support structured data, so we need to join each map into a string,
   then split it again after selection."
  [queues]
  (let [queue-strings (mapv #(str (:messages %) "," (:arn %)) queues)
        selected-queue-arns (->> (fzf {:header {:header-str "Messages,Arn"} :multi true :in queue-strings})
                                 (map #(second (str/split % #","))))]
    (filter #(some #{(:arn %)} selected-queue-arns) queues)))

(defn- receive-all-messages
  "read all messages from a queue"
  [sqs queue-url]
  (let [messages (atom [])
        received (atom [])]
    (while (reset! received (sqs/receive-messages sqs queue-url))
      (swap! messages concat @received))
    @messages))

(defn list-queues
  "list all non-empty dead-letter queues (url and messages)
   
   if --all is provided, also return empty queues"
  [{:keys [all]}]
  (let [client (sqs/client)]
    (->> (cond->> (get-queues-and-attributes client)
           (not all) (filter #(> (:messages %) 0)))
         (map (comp #(assoc % :url (queue-shortname (:url %)))
                    #(dissoc % :arn)))
         (doric/table [{:name :url :title "Queue"} :messages])
         println)))

(defn redrive
  "redrive messages from one or more dead-letter queues and print the resulting move tasks

   if no arguments are provided, select queues interactively using fzf
   if --all is provided, redrive messages from all non-empty dead-letter queues"
  [{:keys [all]}]
  (let [client (sqs/client)
        queues (->> (get-queues-and-attributes client)
                    (filter #(> (:messages %) 0)))
        queues-filtered (cond->> queues
                          (not all) filter-with-fzf)]
    (println (doric/table [:url :messages] queues-filtered))

    (println "\nPress Enter to continue or Ctrl-C to exit")
    (read-line)

    (let [result (->> queues-filtered
                      (pmap #(assoc % :task-handle (sqs/start-message-move-task (sqs/client) (:arn %))))
                      (doall))]
      (println (json/generate-string (map #(dissoc % :messages) result) {:pretty true})))))

(defn read-messages
  [_]
  (let [client (sqs/client)
        queues (->> (get-queues-and-attributes client)
                    (filter #(> (:messages %) 0))
                    (map :url))
        queue-url (fzf {:in queues})
        messages (receive-all-messages client queue-url)]
    (when-not (empty? messages)
      (run! println (map :Body messages)))))

(comment

  (def client (sqs/client))
  (def queues (get-queues-and-attributes client))
;;
  )
