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
    (is (false? (is-valid? (repeat 22 3)))))
  )

