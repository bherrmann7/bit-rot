(ns bit-rot)
(require 'digest)
(use 'clojure.java.io 'clojure.pprint)

(defn get-files [dir]
  (assert (instance? String dir) )
  (vec (.listFiles (file dir)))
  )

(def roots [ "/Users/bob/Desktop/picture-pile/canon_dc" ])
(def roots [ "/Users/bob/Desktop/picture-pile/ip/2015/04"])

(defn walk [dir-stack files]
  (if (empty? dir-stack)
    files
    (let [all-files (get-files (first dir-stack))
          new-dirs (map str (filter #(.isDirectory %) all-files))
          new-files (filter #(not (.isDirectory %)) all-files)]
      (walk (concat (rest dir-stack) new-dirs) (concat files new-files)))
      ))

(def m-files (walk roots []))

(doseq [f m-files]
  (println (str f) (digest/md5 f)))
