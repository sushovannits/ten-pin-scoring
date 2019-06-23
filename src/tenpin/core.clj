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
(defrecord Turn [first-pins second-pins curr-turn-score score])
(defn construct-turns
  "
    Given a set of scores and a cumulative score recursively construct a list of turns
    Each turn will contain first-pins second-pins score
    score is the cumulative score TODO: May be we need individual score of the turn
    
    Note: Here we are not taking into consideration the special case of strike in the last turn or spare in last turn
          so tha this function is recursive and do not have to take into consideration its index
    Logic:
      if strike take the first score -> construct the frame -> construct-turns rest of list (i.e. minus first score)
      else take the first two score -> construct the frame -> construct-turns of list(i.e. minus first two score)
  "
  [prev-score scores]
  (if (empty? scores)
    () ;limit case
    (let [first-score (first scores)
          first-turn-score (turn-score scores)
          cumul-score (+ prev-score first-turn-score)]
      (if (= first-score 10)
        (cons (Turn. first-score nil first-turn-score cumul-score) ; current-turn
              (construct-turns cumul-score (next scores))) ; rest-of-the-turns
        (cons (Turn. first-score (nth scores 1 nil) first-turn-score cumul-score)
              (construct-turns cumul-score (nthnext scores 2)))))))

(construct-turns 0 [0 1 2 3])
(construct-turns 0 [10 10 2 1])
(defn -main [& args] (println "test init"))
