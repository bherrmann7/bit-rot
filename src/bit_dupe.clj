(ns bit-dupe)

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
(def hash-list
  (let [last-rpt-name (last (sort (filter #(.startsWith % "report-") (map str (.list (file (get-dir)))))))
        last-rpt (load-report last-rpt-name)]
    (make-hash-list {} last-rpt)))

#_(doseq [[hash list] hash-list]
    (if (> (count list) 1)
      (if (some #(.contains % "/final-export/") list)
        (println hash (count list) (map #(.substring % (count "/Users/bob/Desktop/picture-pile/")) list)))))

(def has-multi (filter #(> (count (second %)) 1) hash-list))
(defn has-final-export [[hash list]]
  (some #(.contains % "/final-export/") list))
(def has-final (filter has-final-export has-multi))
(def has-not-final (filter #(not (has-final-export %)) has-multi))

(println "Total" (count hash-list))
(println "has-multi" (count has-multi))
(println "has-final" (count has-final))
(println "has-not-final" (count has-not-final))
(println "has-multi:" has-multi)

(defn to-delete [delete-list hash-list]
  (if (empty? hash-list)
    delete-list
    (let [files (filter #(not (.contains % "/final-export/")) (second (first hash-list)))]
      (recur (concat delete-list files) (rest hash-list)))))

#_(doseq [f (to-delete [] has-multi)]
    (io/delete-file f))

#_(println "count" (count (to-delete [] has-final)))

(def files (map #(first (second %)) hash-list))

(def sfile (sort files))

(println "total files" (count sfile))

(def ffiles (map #(.getName (io/file %)) sfile))

(println "total names" (count ffiles))

(println "total unique names" (count (into #{} ffiles)))

(println (take 5 ffiles))
