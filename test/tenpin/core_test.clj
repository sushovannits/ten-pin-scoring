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
