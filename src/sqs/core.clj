(ns sqs.core
  (:require [cheshire.core :as json]
            [clojure.string :as str]
            [crockery.core :as crockery]
            [fzf.core :refer [fzf]]
            [sqs.lib.async :refer [jxmap]]
            [sqs.lib.sqs.core :as sqs-core]))

(defn- get-queues-and-attributes
  "list queues and their attributes

   supported flags
     --non-empty: exclude empty queues
     --dlq: only return dead-letter queues"
  [client]
  (let [queues-messages (sqs-core/get-queues-and-attributes client)]
    (map (fn [{:keys [name url] {:keys [body]} :attributes}]
           {:name name
            :url url
            :arn (:arn body)
            :messages (:messages body)}) queues-messages)))

(defn- filter-with-fzf
  "filter a list of queues interactively with fzf

   fzf doesn't support structured data, so we need to join each map into a string,
   then split it again after selection."
  [queues]
  (let [queue-strings (mapv #(str (:messages %) "," (:name %)) queues)
        selected-queue-names (->> (fzf {:header {:header-str "Messages,Name"} :multi true :in queue-strings})
                                  (map #(second (str/split % #","))))]
    (filter #(some #{(:name %)} selected-queue-names) queues)))

(defn print-queues [opts]
  (let [{:keys [non-empty dlq jira edn]} opts
        client (sqs-core/client)
        queue-messages (get-queues-and-attributes client)
        result (->> (cond->> queue-messages
                      non-empty (filter #(pos-int? (:messages %)))
                      dlq (filter #(sqs-core/dead-letter-queue? (:url %)))))
        table-format (if jira :gfm :fancy)]
    (if edn (prn result)
        (crockery/print-table {:format table-format} [:name :messages] result))))

(defn redrive
  "redrive messages from one or more dead-letter queues and print the resulting move tasks

   if no arguments are provided, select queues interactively using fzf
   if --all is provided, redrive messages from all non-empty dead-letter queues"
  [{:keys [all]}]
  (let [client (sqs-core/client)
        queues (->> (get-queues-and-attributes client)
                    (filter #(pos-int? (:messages %)))
                    (filter #(sqs-core/dead-letter-queue? (:url %))))
        queues-filtered (cond->> queues
                          (not all) filter-with-fzf)]

    (when (empty? queues-filtered)
      (throw (ex-info "No queues selected" {:babashka/exit 1})))

    (crockery/print-table [:name :messages :arn] queues-filtered)

    (println "\nPress Enter to continue or Ctrl-C to exit")
    (read-line)

    (let [result (jxmap #(sqs-core/start-message-move-task client (:arn %)) queues-filtered)]
      (println (json/generate-string result {:pretty true})))))

(defn read-messages
  [_]
  (let [client (sqs-core/client)
        queues (->> (get-queues-and-attributes client)
                    (filter #(> (:messages %) 0))
                    (map :url))
        queue-url (fzf {:in queues})
        messages (sqs-core/receive-all-messages client queue-url)]
    (when-not (empty? messages)
      (run! println (map :Body messages)))))

(comment

  (def qaa (get-queues-and-attributes (sqs-core/client)))
  (get-queues-and-attributes (sqs-core/client))

  (crockery/print-table {:format :gfm}  qaa)
;;
  :rfc)
