(ns sqs.lib.async
  (:import [java.util.concurrent ExecutorService Executors Future]))

(defn jxmap
  "like pmap, but uses the java executors framwork"
  ([f coll] (jxmap f coll 100))
  ([f coll concurrency]
   (let [executor  (Executors/newFixedThreadPool concurrency)
         tasks (mapv #(fn [] (f %)) coll)]
     (->> (.invokeAll ^ExecutorService executor tasks)
          (map #(.get ^Future %))))))

(comment

  (let [f #(inc %)
        coll (range 300)
        concurrency 300]
    (time (println (jxmap f coll concurrency))))

  *e

  :rcf)
