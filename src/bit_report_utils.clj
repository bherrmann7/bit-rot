(ns bit-report-utils)

(use 'bit-utils 'clojure.java.io)
(require 'postal.core 'clojure.pprint)
(require '[clojure.java.io :as io])

; hash the tree for fast lookup
(defn make-hash [hash tree]
  (if (empty? tree)
    hash
    (let [[file m5hash] (first tree)]
      (recur (assoc hash file m5hash) (rest tree)))))

(defn load-report [rpt-name]
  (read-string (slurp (str (get-dir) "/" rpt-name))))

#_(let [last-rpt-name (last (sort (filter #(.startsWith % "report-") (map str (.list (file (get-dir)))))))]
    last-rpt (load-report last-rpt-name)
    hashes (into #{} (map second last-rpt))
    (println last-rpt-name "last-report" (count last-rpt) (count hashes)))

(defn make-hash-list [hash-list entries]
  (if (empty? entries) hash-list
                       (let [[path hash] (first entries)
                             value (get hash-list hash)
                             new-value (if value value [])]
                         (recur (assoc hash-list hash (conj value path)) (rest entries)))))

;(def some-files [["/a/f" "55"] ["/b" "66"] ["/d/g" "55"]])
#_(clojure.pprint/pprint some-files)
#_(clojure.pprint/pprint (make-hash-list {} some-files))


; detect dirs with identical contents.
(defn create-hash-list []
  (let [last-rpt-name (last (sort (filter #(.startsWith % "report-") (map str (.list (file (get-dir)))))))
        last-rpt (load-report last-rpt-name)]
    (make-hash-list {} last-rpt)))

