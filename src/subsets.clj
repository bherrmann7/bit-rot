(ns subsets)

(use 'bit-utils 'clojure.java.io)
(require 'postal.core 'clojure.pprint 'bit-report-utils)
(require '[clojure.java.io :as io])

(def hash-list (bit-report-utils/create-hash-list))

(def has-multi (filter #(> (count (second %)) 1) hash-list))

(defn extract-dir [fstr]
  (.toString (.getParent (io/file fstr))))

(defn extract-dirs [dirs hash-entry]
  (reduce conj dirs (map extract-dir (second hash-entry))))

(def dirs-containing-duplicates
  (reduce extract-dirs #{} has-multi))

#_(doseq [dup (sort dirs-containing-duplicates)]
    (println dup))

#_(def adir (first (sort dirs-containing-duplicates)))

(defn has-f [file hash-entry]
  (some #(= file %) (second hash-entry)))

(defn find-matching-entry [f]
  (filter #(has-f f %) hash-list))

(defn find-others [f]
  (filter #(not= f %) (second (first (find-matching-entry f)))))

;(println (find-matching-entry "/Users/bob/Desktop/picture-pile/Kokack Zi8/945EKZi8/945_0150.JPG"))
;(println (find-others "/Users/bob/Desktop/picture-pile/Kokack Zi8/945EKZi8/945_0150.JPG"))
(defn dir-has [f]
  (if (nil? f) "-"
      (count (.listFiles (.getParentFile (io/file f))))))

(doseq [adir (sort dirs-containing-duplicates)]
  (println adir (count (.list (io/file adir))))
  (doseq [f (.listFiles (io/file adir))]
    (let [others (first (find-others (.toString f)))]
      (println "  " (.toString f) " " others " " (dir-has others)))))
