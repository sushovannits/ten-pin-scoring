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
   It also returns the type os the turn like :strike :spare :open or :incomplete
   but probably its not good by single responsibility pattern
  "
  [scores]
  (let [total-scores (count scores)]
    (cond
      (= total-scores 0) [:not-started 0]
      (= total-scores 1) [(if (= (first scores) 10) :strike :incomplete) (first scores)]
      (= (first scores) 10) [:strike (apply + (take 3 scores))]  ; strike
      :else (let [sum (reduce + 0 (take 2 scores))]
              (if (= sum 10)
                [:spare (+ sum (nth scores 2 0))]  ; spare
                [:open sum] ; open
)))))

(defrecord Turn [first-pins second-pins curr-turn-score score turn-type])
(defn construct-turns
  "
    Given a set of scores and a cumulative score recursively construct a list of turns
    Each turn will contain first-pins second-pins turn-score cumulative-score turn-type
    score is the cumulative score 
    
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
          [first-turn-type first-turn-score] (turn-score scores)
          cumul-score (+ prev-score first-turn-score)]
      (if (= first-score 10)
        (cons (Turn. first-score nil first-turn-score cumul-score first-turn-type) ; current-turn
              (construct-turns cumul-score (next scores))) ; rest-of-the-turns
        (cons (Turn. first-score (nth scores 1 nil) first-turn-score cumul-score first-turn-type)
              (construct-turns cumul-score (nthnext scores 2)))))))

(defn- resolve-extra-turns
  "
    Incase of an extra turn i.e. when players get extra chance due to a last strike or last spare
    the construct-frame generates an extra turn. This function consolidates it to the last turn 
    with an extra key of :third-pins
  "
  [turns]
  (if (= (count turns) 11)
    (let [extra-turn (last turns)
          extra-turn-first (:first-pins extra-turn)
          extra-turn-second (:second-pins extra-turn)
          actual-last-turn (last (drop-last 1 turns))
          actual-last-turn-second (:second-pins actual-last-turn)
          last-trun (cond
                      (nil? actual-last-turn-second) (assoc-in
                                                      (assoc-in actual-last-turn [:second-pins] extra-turn-first)
                                                      [:third-pins] extra-turn-second) ; strike
                      :else ; spare
                      (assoc-in actual-last-turn [:third-pins] extra-turn-first))]
      (concat (drop-last 2 turns) [last-trun]))
    turns))

(defn over?
  "
    Count of frame if less than 10 then definitely game is not over
    If 10 then we need to find out if the 2 scores or 3 scores(in case of spare or strike) is not nil
  "
  [frames]
  (let [c (count frames)]
    (cond
      (< c 10) false
      :else
      (let [tail (nthnext frames 9)
            tenth-frame (first tail)
            {f :first-pins s :second-pins t :third-pins} tenth-frame
            is-strike (= f 10)
            is-spare (= (+ f (or s 0)) 10)]
        (cond
          is-strike (every? (comp not nil?) [f s t])
          is-spare (every? (comp not nil?) [f s t])
          :else false)))))

(defn- compute-score-card
  "
    Constructs the score card for a given list of scores
  "
  [scores]
  (if (is-valid? scores)
    (let [turns (resolve-extra-turns (construct-turns 0 scores))
          score-card {:turns  turns
                      :status (cond
                                (= (count scores) 0) :not-started
                                (over? turns) :over
                                :else :on-going)}]
      score-card)
    [nil, "Error"]))

(compute-score-card [1 9 2 4 10 10 10 7 1 2 8 7 0 0 0 10 1 9])
(defn -main [& args] (println "test init"))
