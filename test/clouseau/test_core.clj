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

(deftest test-start-server-existence
    "Check that the clouseau.core/start-server definition exists."
    (testing "if the clouseau.core/start-server definition exists."
        (is (callable? 'clouseau.core/start-server))))

(deftest test-get-port-existence
    "Check that the clouseau.core/get-port definition exists."
    (testing "if the clouseau.core/get-port definition exists."
        (is (callable? 'clouseau.core/get-port))))

(deftest test-cli-options-def-existence
    "Check that the clouseau.core/cli-options definition exists."
    (testing "if the hostname def exists"
        ; check if variable exists and it's bounded
        (is (bound? (find-var 'clouseau.core/cli-options)))))

(deftest test-app-def-existence
    "Check that the clouseau.core/app definition exists."
    (testing "if the hostname def exists"
        ; check if variable exists and it's bounded
        (is (bound? (find-var 'clouseau.core/app)))))

(deftest test-default-port-def-existence
    "Check that the clouseau.core/default-port definition exists."
    (testing "if the hostname def exists"
        ; check if variable exists and it's bounded
        (is (bound? (find-var 'clouseau.core/default-port)))))

;
; Tests for function behaviours
;

(deftest test-get-port
    "Check the function clouseau.core/get-port."
    (testing "the function clouseau.core/get-port."
        (is (= (get-port nil)    "3000"))
        (is (= (get-port "")    "3000"))
        (is (= (get-port "1")    "1"))
        (is (= (get-port "3000") "3000"))))

