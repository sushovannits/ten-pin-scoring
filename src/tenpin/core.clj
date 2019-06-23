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

(defn turn-score
  " Given a list of valid scores calculate the score of the first turn
    - if first score is a strike then it should look ahead for two next scores
    - if first two score is a spare then look ahead for just one more score 
    - if open then just sum two score
  "
  [scores]
  (let [total-scores (count scores)]
    (cond
      (= total-scores 0) 0
      (= total-scores 1) (first scores)
      (= (first scores) 10) (apply + (take 3 scores))  ; strike
      :else (let [sum (reduce + 0 (take 2 scores))]
              (if (= sum 10)
                (+ sum (nth scores 2 0))  ; spare
                sum ; open
)))))

(defn -main [& args] (println "test init"))
