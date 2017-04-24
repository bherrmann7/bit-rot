(ns bit-utils)
(use 'clojure.java.io 'clojure.pprint)

(def bit-rot-dir (str (System/getProperty "user.home") "/.bit-rot"))

(defn error [& message]
  (apply println message)
  (System/exit 1))

(defn get-dir []
  (if (not (.exists (file bit-rot-dir)))
    (error "ERROR missing bit rot dir:" bit-rot-dir)
    bit-rot-dir))

(def _config nil)

(defn get-config []
  (if _config _config
      (let [config-file (str bit-rot-dir "/config")]
        (if (not (.exists (file config-file)))
          (error "ERROR missing config file:" config-file))
        (def _config (read-string (slurp config-file)))
        _config)))

(defn get-roots []
  (def roots (:roots (get-config)))

  (def fmt "FORMAT: {:roots [ \"collectionid\" \"/path/to/tree\"])\n");
  (def example "EXAMPLE: {:roots [\"pict-pile\" \"/Users/bob/Desktop/picture-pile/ip/2015/04\"]}\n")

  (if (empty? roots)
    (error "ERROR no roots in config file\n" fmt example))

  (if (= 1 (mod (count roots) 2))
    (error "roots should be an even pairs of id and dir.\n" fmt example))

  roots)

