(ns bit-hash)
(require 'digest)
(use 'clojure.java.io 'clojure.pprint 'bit-utils)

(defn get-files [dir]
  (assert (instance? String dir))
  (vec (.listFiles (file dir))))

(defn walk [dir-stack files]
  ;(println "considering : " dir-stack)
  (if (empty? dir-stack)
    files
    (let [all-files (get-files (first dir-stack))
          new-dirs (map str (filter #(.isDirectory %) all-files))
          new-files (filter #(not (.isDirectory %)) all-files)]
      (recur (concat (rest dir-stack) new-dirs) (concat files new-files)))))

(defn gen-next-report-filename [bit-rot-dir]
  (let [date-part (.format (java.text.SimpleDateFormat. "yyyyMMdd-HHmm") (java.util.Date.))]
    (str bit-rot-dir "/report-" date-part ".out")))

(def rpt-out (gen-next-report-filename (get-dir)))

(def m-files (walk (get-roots) []))

(println "Total files to hash:" (count m-files))

(def hashed (map #(vector (str %1) (digest/md5 %1)) m-files))

(with-open [w (clojure.java.io/writer rpt-out)]
  (binding [*out* w]
    (pr hashed)))
