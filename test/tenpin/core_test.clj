(ns tenpin.core-test
  (:require [clojure.test :refer :all]
            [tenpin.core :refer :all])
  (:import [tenpin.core Turn]))

(deftest test-is-valid?
  (testing "empty scores"
    (is (true? (is-valid? ()))))
  (testing "one score"
    (is (true? (is-valid? '(1)))))
  (testing "invalid score"
    (is (false? (is-valid? '(11)))))
  (testing "with two valid"
    (is (true? (is-valid? '(1 2)))))
  (testing "with two invalid"
    (is (false? (is-valid? '(4 8)))))
  (testing "a strike score"
    (is (true? (is-valid? '(10 8)))))
  (testing "with too many rolls"
    (is (false? (is-valid? (repeat 22 3))))))

(deftest test-turn-score
  (testing "empty scores"
    (is (= 0 ((comp second turn-score) '()))))
  (testing "with one score"
    (is (= 1 ((comp second turn-score) '(1)))))
  (testing "with two rolls"
    (is (= 3 ((comp second turn-score) '(1 2)))))
  (testing "Incomplete spare"
    (is (= 10 ((comp second turn-score) '(3 7)))))
  (testing "Complete spare"
    (is (= 12 ((comp second turn-score) '(3 7 2)))))
  (testing "Incomplete strike"
    (is (= 15 ((comp second turn-score) '(10 5)))))
  (testing "Complete strike"
    (is (= 19 ((comp second turn-score) '(10 2 7)))))
  (testing "Max turn score"
    (is (= 30 ((comp second turn-score) '(10 10 10 2)))))
  (testing "Strike and spare"
    (is (= 20 ((comp second turn-score) '(10 9 1 4))))))

(deftest test-construct-turns
  (testing "empty scores"
    (is (empty? (construct-turns 0 '()))))
  (testing "incomplete strike"
    (is (= (list (Turn. 10 nil 20 20 :strike) (Turn. 10 nil 10 30 :strike))
           (construct-turns 0 [10 10]))))
  (testing "complccte strike"
    (is (= (list (Turn. 10 nil 22 22 :strike) (Turn. 10 nil 12 34 :strike) (Turn. 2 0 2 36 :open))
           (construct-turns 0 [10 10 2 0]))))
  (testing "incomplete spare"
    (is (= (list (Turn. 9 1 10 10 :spare))
           (construct-turns 0 [9 1]))))
  (testing "complete spare"
    (is (= (list (Turn. 9 1 14 14 :spare) (Turn. 4 nil 4 18 :incomplete))
           (construct-turns 0 [9 1 4]))))
  (testing "open"
    (is (= (list (Turn. 8 1 9 9 :open) (Turn. 4 0 4 13 :open))
           (construct-turns 0 [8 1 4 0])))))

(deftest test-find-scores
  (testing "testing empty"
    (is (= [nil nil nil]
           (find-scores (repeat 0 (Turn. 10 nil 0 0 :strike))))))
  (testing "turns with three strikes at end"
    (is (= [10 10 10]
           (find-scores (repeat 3 (Turn. 10 nil 0 0 :strike))))))
  (testing "not enough turns should pad with nil"
    (is (= [10 nil nil]
           (find-scores (repeat 1 (Turn. 10 nil 0 0 :strike)))))))

(def last-turn-strike (concat (repeat 9 (Turn. 1 2 0 0 :open)) [(Turn. 10 nil 0 0 :strike) (Turn. 4 5 0 0 :open)]))
(def extra-tail (repeat 12 (Turn. 1 2 0 0 :open)))
(def last-turn-open (concat (repeat 9 (Turn. 1 2 0 0 :open)) [(Turn. 4 5 0 0 :open)]))
(def last-turn-spare (concat (repeat 9 (Turn. 1 2 0 0 :open)) [(Turn. 5 5 0 0 :spare) (Turn. 6 7 0 0 :open)]))

(deftest test-resolve-extra-turns
  (testing "testing extra turns are consolidated when last turn is a strike"
    (is (=  (into {} (last (resolve-extra-turns last-turn-strike)))
            {:first-pins 10 :second-pins 4 :third-pins 5 :curr-turn-score 0 :score 0 :turn-type :strike})))
  (testing "testing extra turns are consolidated when last turn is a open"
    (is (=  (into {} (last (resolve-extra-turns last-turn-open)))
            {:first-pins 4 :second-pins 5 :curr-turn-score 0 :score 0 :turn-type :open})))
  (testing "testing extra turns are consolidated when last turn is a spare"
    (is (=  (into {} (last (resolve-extra-turns last-turn-spare)))
            {:first-pins 5 :second-pins 5 :curr-turn-score 0 :score 0 :turn-type :spare :third-pins 6})))
  (testing "testing extra turns are dropped"
    (is (=  (count (resolve-extra-turns extra-tail))
            10))
    (is (=  (into {} (last (resolve-extra-turns extra-tail)))
            {:first-pins 1 :second-pins 2 :curr-turn-score 0 :score 0 :turn-type :open}))))

(deftest test-compute-score-card
  (testing "empty scores"
    (is (=  (:turns (compute-score-card []))
            ()))
    (is (= (:status (compute-score-card []))
           :not-started)))
  (testing "one turn played"
    (is (=  (:score (last (:turns (compute-score-card [1 2]))))
            3))
    (is (= (:status (compute-score-card [1 2]))
           :on-going)))
  (testing "two turn played"
    (is (=  (:score (last (:turns (compute-score-card [1 2 3 4]))))
            10)))
  (testing "all turn played with all strikes"
    (is (=  (:score (last (:turns (compute-score-card (repeat 12 10)))))
            300))
    (is (= (:status (compute-score-card (repeat 12 10)))
           :over)))
  (testing "all turn played with all strikes and a open in end"
    (is (=  (:score (last (:turns (compute-score-card (concat (repeat 10 10) [1 2])))))
            274))
    (is (= (:status (compute-score-card (concat (repeat 10 10) [1 2])))
           :over))
    (is (= (:status (compute-score-card (concat (repeat 10 10) [1])))
           :on-going)))
  (testing "all turn played with all spares and last strike"
    (is (=  (:score (last (:turns (compute-score-card (concat (flatten (repeat 10 [8 2])) [10])))))
            182))
    (is (= (:status (compute-score-card (concat (flatten (repeat 10 [8 2])) [10])))
           :over))
    (is (= (:status (compute-score-card (flatten (repeat 10 [8 2])))) ; when incomplete
           :on-going))))
