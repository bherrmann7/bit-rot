(ns dt)
(require '[bit-report-utils :as bru]
         '[datomic.api :as d]
         '[clojure.pprint])

(defn print-schema [db schema-prefix]
  (doseq [x  (filter #(.startsWith (namespace (first %)) schema-prefix)
                     (d/q
                      '[:find ?attr ?type ?card
                        :where
                        [_ :db.install/attribute ?a]
                        [?a :db/valueType ?t]
                        [?a :db/cardinality ?c]
                        [?a :db/ident ?attr]
                        [?t :db/ident ?type]
                        [?c :db/ident ?card]]
                      db))]
    (clojure.pprint/pprint x)))

(defn print-data [db]
  (doseq [file (d/q '[:find ?name :where [_ :file/name ?name]] db)]
    (println file)))

(defn load-data [conn data]
  @(d/transact conn (map #(hash-map :file/name (first %) :file/hash (second %)) data)))

(def db-uri "datomic:mem://photo-rec")

(def assets #{"540f65d2b2f94032d6c3037622843a50" "82e25a810f86d3b8ca0ca42ef56a8956" "110afaaaf901f67fee2357c5df88a1aa" "804543daa860e91aabe9ba634e64ed2b" "1dc00c5b03f7d1b19360666efdc24d5d" "64be74b6e70c6e8aadf04088462ba5d4" "ef26ef0081ed01613cf6fa8b68019ca4" "5bb9df0d5636d6dc9356abec3092b4b5" "167e33a437a067c72fc4a58114506004"
              "fc94fb0c3ed8a8f909dbc7630a0987ff" "696bd054b0069b60748474abb87b28b7" "fb5fff30d471cc603589578015d36864" "7e99e1159a3686f6aa4f90043c554483"
              "ec266084ece29ede795db38c9c8cbf3d" "0e5462b0b4f00432eac4b33d5fa31c5a" "2059be04139e24558449e53920e89924" "9b1b923cd76561359f8886033a4f5848"})

(defn load-photos []
  (let [photo-records-report (bru/load-latest-report)
        _ (println "Loaded"  (count photo-records-report) "raw photo records")
        photo-records-no-assets (filter #(not (contains? assets (second %))) photo-records-report)
        _ (println "Loaded"  (count photo-records-no-assets) "with no assets photo records")
        photo-records-no-del  (filter #(.exists (clojure.java.io/as-file (first %))) photo-records-no-assets)
        _ (println "Loaded"  (count photo-records-no-assets) "with no missing (deleted) files")
        photo-records (filter #(not= "smaller" (.getName (.getParentFile (clojure.java.io/as-file (first %))))) photo-records-no-del)]
    (println "Loaded" (count photo-records) "photo records (no assets/no delete files/no smaller)")
    (d/create-database db-uri)
    (let [conn (d/connect db-uri)
          db (d/db conn)]
      (println "datoms count " (count (seq (d/datoms db :aevt))))
      (def schema
        [{:db/ident :file/name :db/valueType :db.type/string :db/cardinality :db.cardinality/one}
         {:db/ident :file/hash :db/valueType :db.type/string :db/cardinality :db.cardinality/one}])
      @(d/transact conn schema)
      (load-data conn photo-records)
      (let [db (d/db conn)]
        (println "datoms count " (count (seq (d/datoms db :aevt)))))
      "loaded")))

(defn show-schema []
  (let [conn (d/connect db-uri)
        db (d/db conn)]
    (println "datoms count" (count (seq (d/datoms db :aevt))))
    (print-schema db "file")))

(defn shorten-path [x] (.substring ^String x 32))

(defn get-dups []
  (let [start-time (System/currentTimeMillis)
        conn (d/connect db-uri)
        db (d/db conn)
        dups (d/q '[:find ?name ?name2 ?hash :where [?e :file/name ?name] [?e :file/hash ?hash] [?j :file/hash ?hash] [?j :file/name ?name2] [(< ?name ?name2)]] db)
        dups (filter #(not (contains? assets (get % 2))) dups)
        end-time (System/currentTimeMillis)]
    (printf "Took %d seconds%n" (int (/ (- end-time start-time) 1000)))
    dups));(map #(vector (shorten-path (first %)) (shorten-path (second %)) (get % 2)) dups)

(defn pdir [x] (let [lastSlashLoc (inc (.lastIndexOf x "/"))]
                 (.substring x 0 lastSlashLoc)))

(defn dupe-dir [dups]
  (reduce #(conj %1 (pdir (first %2)) (pdir (second %2))) #{} dups))

;(def conn (d/connect db-uri))
;(def db (d/db conn))

(defn find-files-start-with [db x]
  (d/q '[:find ?name :in $ ?x :where [_ :file/name ?name] [(.startsWith ^String ?name ?x)]] db x))

(defn find-with-dups-from-dir [dups dir] (filter #(or (.startsWith (first %) dir) (.startsWith (second %) dir))  dups))

(defn rpt [db dups]
  (printf "%4s %4s %s%n" "TOT" "DUPS" "Base Directory")
  (doseq [x (dupe-dir dups)] (printf "%4d %4d %s%n" (count (find-files-start-with db x)) (count (find-with-dups-from-dir dups x)) x)))

#_(defn disp-dups [dups]
    (doseq [x dups] (printf "%.30s %-50s %s%n" (get x 2) (shorten-path (first x)) (shorten-path (second x)))))

(defn disp-dups [dups]
  (doseq [x dups] (printf "%.30s %-50s %s%n" (get x 2) (first x) (second x))))

(comment
  (load "dt") (in-ns 'dt) (load-photos) (def dups (get-dups))
  (def conn (d/connect db-uri))
  (def db (d/db conn))
  (rpt db dups)
  "this is here to bring the paren down... so the other lines can be cut/paste into the repl")

