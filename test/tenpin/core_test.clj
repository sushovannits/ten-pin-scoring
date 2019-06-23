(ns tenpin.core-test
  (:require [clojure.test :refer :all]
            [tenpin.core :refer :all]))

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
  (testing "empty list"
    (is (= 0 (turn-score '()))))
  (testing "with one score"
    (is (= 1 (turn-score '(1)))))
  (testing "with two rolls"
    (is (= 3 (turn-score '(1 2)))))
  (testing "Incomplete spare"
    (is (= 10 (turn-score '(3 7)))))
  (testing "Complete spare"
    (is (= 12 (turn-score '(3 7 2)))))
  (testing "Incomplete strike"
    (is (= 15 (turn-score '(10 5)))))
  (testing "Complete strike"
    (is (= 19 (turn-score '(10 2 7)))))
  (testing "Max turn score"
    (is (= 30 (turn-score '(10 10 10 2)))))
  (testing "Strike and spare"
    (is (= 20 (turn-score '(10 9 1 4))))))
