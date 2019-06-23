(ns tenpin.core
  (:gen-class))

(defn valid-score?
  [score]
  (and (>= score 0)
       (<= score 10)))

(defn is-valid?
  " Given a list of scores validates the list
    - check the toal length 21
    - if strike then check only one score 
    - if spare/open then check two scores
    - recursively check the rest of the list  
  "
  [scores]
  (let [total-scores (count scores)]
    (cond
      (> total-scores 21) false
      (= total-scores 0) true
      (= total-scores 1) (valid-score? (first scores))
      (= (first scores) 10) (is-valid? (next scores))
      (let [[f s] (take 2 scores)]
        (and (valid-score? f) (valid-score? s) (valid-score? (+ f s))))
      (is-valid? (nthnext scores 2))
      :else false)))

(defn -main [& args] (println "test init"))
