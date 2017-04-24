(ns bit-diff)
(use 'bit-utils 'clojure.java.io)
(require 'postal.core)

; hash the tree for fast lookup
(defn make-hash [hash tree]
  (if (empty? tree)
    hash
    (let [[file m5hash] (first tree)]
      (recur (assoc hash file m5hash) (rest tree)))))

(defn load-report [rpt-name]
  (read-string (slurp (str (get-dir) "/" rpt-name))))

(let [[last-rpt second-last-rpt] (take 2 (reverse (sort (filter #(.startsWith % "report-") (map str (.list (file (get-dir))))))))
      list-older (load-report second-last-rpt)
      list-newer (load-report last-rpt)
      lookup-newer-by-name (make-hash {} list-newer)
      lookup-older-by-name (make-hash {} list-older)
      all-files (into (sorted-set) (concat (map first list-older) (map first list-newer)))
      report (flatten ["\n=== Difference in File Hashes ===\n\n"
                       "comparing newer " last-rpt (count list-newer) "with older" second-last-rpt (count list-older) "\n\n"
                       "Total files in both " (count all-files) "\n\n"
                       (map
                        #(let [older-m5hash (get lookup-older-by-name %)
                               newer-m5hash (get lookup-newer-by-name %)]
                           (if (not= older-m5hash newer-m5hash)
                             [% " " older-m5hash " " newer-m5hash "\n"] "")) all-files)])]
  (System/exit
   (if (:SUCCESS (let [email-auth (:email-auth (get-config))
                       email-user (:user email-auth)]
                   (postal.core/send-message email-auth
                                             {:from    email-user
                                              :to      email-user
                                              :subject "Bit Rot Report"
                                              :body    (apply str report)})))
     0 1)))