(ns clouseau.test-core
  (:require [clojure.test :refer :all]
            [clouseau.core :refer :all]))

;
; Common functions used by tests.
;

(defn callable?
    "Test if given function-name is bound to the real function."
    [function-name]
    (clojure.test/function? function-name))

;
; Tests for various defs and functions
;

(deftest test-main-existence
    "Check that the clouseau.core/-main definition exists."
    (testing "if the clouseau.core/-main definition exists."
        (is (callable? 'clouseau.core/-main))))

