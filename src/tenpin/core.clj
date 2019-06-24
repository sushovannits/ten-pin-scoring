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

(defn- find-scores
  "
  This function basically takes extra turns and consolidates into the last frame
  So if we get a last frame as [10 nil] and then 3 other turns as [10 nil] [10 nil] [3 nil]
  Then [3 nil] frame must be discarded and the actual last frame should be [10 10 10]
  This function finds this valid list of pin numbers to construct the last frame in case of 
  "
  [turns]
  (first (partition 3 3 (repeat nil) (remove nil? (mapcat (fn [turn] (vals (select-keys turn [:first-pins :second-pins]))) turns)))))

; (find-scores [{:f 1 :s nil} {:f 2 :s nil} {:f 3 :s 7}])

(defn- strike-or-spare?
  "finds if a trun is strike or spare"
  [{:keys [first-pins second-pins]}]
  (let
   [f (or first-pins 0)
    s (or second-pins 0)]
    (or
     (= f 10)
     (= (+ f s) 10))))

; (strike-or-spare? {:first-pins 2 :second-pins 2})

(defn- resolve-extra-turns
  "
    Incase of an extra turn i.e. when players get extra chance due to a last strike or last spare
    the construct-frame generates an extra turn. This function consolidates it to the last turn 
    with an extra key of :third-pins
  "
  [turns]
  (if (>= (count turns) 11) ; extra scores are present
    (if (strike-or-spare? (nth turns 9))
      (let [[extra-turn-first extra-turn-second extra-turn-third] (find-scores (nthnext turns 9))
            actual-last-turn (nth turns 9)
            last-turn (-> actual-last-turn
                          (assoc-in [:first-pins] extra-turn-first)
                          (assoc-in [:second-pins] extra-turn-second)
                          (assoc-in [:third-pins] extra-turn-third))]
        (concat (take 9 turns) [last-turn]))
      (take 10 turns))
    turns))
(resolve-extra-turns (repeat 10 {:first-pins 1 :second-pins 2}))

(defn over?
  "
    Count of frame if less than 10 then definitely game is not over
    If 10 then we need to find out if the 2 scores or 3 scores(in case of spare or strike) is not nil
  "
  [turns]
  (let [c (count turns)]
    (cond
      (< c 10) false
      :else
      (let [tail (nthnext turns 9)
            tenth-frame (first tail)
            {f :first-pins s :second-pins t :third-pins} tenth-frame
            is-strike (= f 10)
            is-spare (= (+ f (or s 0)) 10)]
        (cond
          is-strike (every? (comp not nil?) [f s t])
          is-spare (every? (comp not nil?) [f s t])
          :else (every? (comp not nil?) [f s]))))))

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

(compute-score-card [1 9 2 4 10 10 10 7 1 2 8 7 0 0 0 10 1 9]) ; should be 145
(compute-score-card (concat (repeat 9 10) (flatten (repeat 4 [1 2])))) ; should be 247 
(compute-score-card (concat (repeat 9 10) [1 2])) ; should be 247 
(compute-score-card (repeat 12 10)) ; 300 
(compute-score-card (concat  (repeat 10 10) [1 2 3 4])) ; 274
(defn -main [& args] (println "test init"))
