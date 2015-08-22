;;;
;;;   Clouseau
;;; 
;;;    Copyright (C) 2015 Pavel Tisnovsky <ptisnovs@redhat.com>
;;; 
;;; Clouseau is free software; you can redistribute it and/or modify
;;; it under the terms of the GNU General Public License as published by
;;; the Free Software Foundation; either version 2, or (at your option)
;;; any later version.
;;; 
;;; Clouseau is distributed in the hope that it will be useful, but
;;; WITHOUT ANY WARRANTY; without even the implied warranty of
;;; MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
;;; General Public License for more details.
;;; 
;;; You should have received a copy of the GNU General Public License
;;; along with Clouseau; see the file COPYING.  If not, write to the
;;; Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
;;; 02110-1301 USA.
;;; 
;;; Linking this library statically or dynamically with other modules is
;;; making a combined work based on this library.  Thus, the terms and
;;; conditions of the GNU General Public License cover the whole
;;; combination.
;;; 
;;; As a special exception, the copyright holders of this library give you
;;; permission to link this library with independent modules to produce an
;;; executable, regardless of the license terms of these independent
;;; modules, and to copy and distribute the resulting executable under
;;; terms of your choice, provided that you also meet, for each linked
;;; independent module, the terms and conditions of the license of that
;;; module.  An independent module is a module which is not derived from
;;; or based on this library.  If you modify this library, you may extend
;;; this exception to your version of the library, but you are not
;;; obligated to do so. If you do not wish to do so, delete this
;;; exception statement from your version.
;;; 

(ns clouseau.test-core
  (:require [clojure.test :refer :all]
            [clouseau.core :refer :all]
            [ring.adapter.jetty :as jetty]))

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

(deftest test-get-and-check-port-existence
    "Check that the clouseau.core/get-and-check-port definition exists."
    (testing "if the clouseau.core/get-and-check-port definition exists."
        (is (callable? 'clouseau.core/get-and-check-port))))

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
        (is (= (get-port "1")     "1"))
        (is (= (get-port "2")     "2"))
        (is (= (get-port "3000")  "3000"))
        (is (= (get-port "65534") "65534"))
        (is (= (get-port "65535") "65535"))))

(deftest test-get-port-special-cases
    "Check the function clouseau.core/get-port."
    (testing "the function clouseau.core/get-port."
        (is (= (get-port nil)     "3000"))
        (is (= (get-port "")      "3000"))
        (is (= (get-port 0)       "3000"))
        (is (= (get-port 1)       "3000"))
        (is (= (get-port 65535)   "3000"))
        (is (= (get-port 65536)   "3000"))))

(deftest test-get-port-negative
    "Check the function clouseau.core/get-port."
    (testing "the function clouseau.core/get-port."
        (is (thrown? AssertionError (get-port "0")))
        (is (thrown? AssertionError (get-port "-1")))
        (is (thrown? AssertionError (get-port "-2")))
        (is (thrown? AssertionError (get-port "65536")))
        (is (thrown? AssertionError (get-port "65537")))
        (is (thrown? AssertionError (get-port "1000000")))))

(deftest test-get-and-check-port
    "Check the function clouseau.core/get-and-check-port."
    (testing "the function clouseau.core/get-and-check-port."
        (is (= (get-and-check-port "1")     "1"))
        (is (= (get-and-check-port "2")     "2"))
        (is (= (get-and-check-port "65534") "65534"))
        (is (= (get-and-check-port "65535") "65535"))))

(deftest test-get-and-check-port-negative
    "Check the function clouseau.core/get-and-check-port."
    (testing "the function clouseau.core/get-and-check-port."
        (is (thrown? AssertionError (get-and-check-port "-1")))
        (is (thrown? AssertionError (get-and-check-port "0")))
        (is (thrown? AssertionError (get-and-check-port "65536")))
        (is (thrown? AssertionError (get-and-check-port "65537")))
        (is (thrown? AssertionError (get-and-check-port "1000000")))))

(deftest test-start-server-positive-1
    (testing "clouseau.core/start-server"
        ; use mock instead of jetty/run-jetty
        (with-redefs [jetty/run-jetty (fn [app port] port)]
            (is (= {:port 1}     (start-server "1")))
            (is (= {:port 2}     (start-server "2")))
            (is (= {:port 1000}  (start-server "1000")))
            (is (= {:port 65534} (start-server "65534")))
            (is (= {:port 65535} (start-server "65535"))))))

(deftest test-start-server-positive-2
    (testing "clouseau.core/start-server"
        ; use mock instead of jetty/run-jetty
        (with-redefs [jetty/run-jetty (fn [app port] app)]
            (is (= app (start-server "1")))
            (is (= app (start-server "2")))
            (is (= app (start-server "1000")))
            (is (= app (start-server "65534")))
            (is (= app (start-server "65535"))))))

(deftest test-start-server-negative
    (testing "clouseau.core/start-server"
        ; use mock instead of jetty/run-jetty
        (with-redefs [jetty/run-jetty (fn [app port] port)]
            (is (= {:port -1}      (start-server "-1")))
            (is (= {:port 0}       (start-server "0")))
            (is (= {:port 65536}   (start-server "65536")))
            (is (= {:port 65537}   (start-server "65537")))
            (is (= {:port 1000000} (start-server "1000000"))))))

(deftest test-main
    (testing "clouseau.core/-main"
        ; use mock instead of jetty/run-jetty
        (with-redefs [jetty/run-jetty (fn [app port] port)]
            (is (= {:port 3000}    (-main)))
            (is (= {:port 3000}    (-main "-1")))
            (is (= {:port 3000}    (-main "-p" "3000")))
            (is (= {:port 9999}    (-main "-p" "9999")))
            )))

