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

(ns clouseau.test-db-interface
  (:require [clojure.test           :refer :all]
            [clouseau.db-interface  :refer :all]
            [clojure.java.jdbc      :as jdbc]
            [clouseau.db-spec       :as db-spec]))

;
; Common functions used by tests.
;

(defn callable?
    "Test if given function-name is bound to the real function."
    [function-name]
    (clojure.test/function? function-name))

;
; Tests for various functions
;

(deftest test-read-description-existence
    "Check that the clouseau.db-interface/read-description definition exists."
    (testing "if the clouseau.db-interface/read-description definition exists."
        (is (callable? 'clouseau.db-interface/read-description))))

(deftest test-read-changes-statistic-existence
    "Check that the clouseau.db-interface/read-changes-statistic definition exists."
    (testing "if the clouseau.db-interface/read-changes-statistic definition exists."
        (is (callable? 'clouseau.db-interface/read-changes-statistic))))

(deftest test-read-changes-existence
    "Check that the clouseau.db-interface/read-changes definition exists."
    (testing "if the clouseau.db-interface/read-changes definition exists."
        (is (callable? 'clouseau.db-interface/read-changes))))

(deftest test-read-changes-for-user-existence
    "Check that the clouseau.db-interface/read-changes-for-user definition exists."
    (testing "if the clouseau.db-interface/read-changes-for-user definition exists."
        (is (callable? 'clouseau.db-interface/read-changes-for-user))))

(deftest test-read-ccs-description-existence
    "Check that the clouseau.db-interface/read-ccs-description definition exists."
    (testing "if the clouseau.db-interface/read-ccs-description definition exists."
        (is (callable? 'clouseau.db-interface/read-ccs-description))))

(deftest test-read-all-descriptions-existence
    "Check that the clouseau.db-interface/read-all-descriptions definition exists."
    (testing "if the clouseau.db-interface/read-all-descriptions definition exists."
        (is (callable? 'clouseau.db-interface/read-all-descriptions))))

(deftest test-read-all-packages-provided-by-ccs-existence
    "Check that the clouseau.db-interface/read-all-packages-provided-by-ccs definition exists."
    (testing "if the clouseau.db-interface/read-all-packages-provided-by-ccs definition exists."
        (is (callable? 'clouseau.db-interface/read-all-packages-provided-by-ccs))))

(deftest test-store-ccs-description-existence
    "Check that the clouseau.db-interface/store-ccs-description definition exists."
    (testing "if the clouseau.db-interface/store-ccs-description definition exists."
        (is (callable? 'clouseau.db-interface/store-ccs-description))))

(deftest test-store-changes-existence
    "Check that the clouseau.db-interface/store-changes definition exists."
    (testing "if the clouseau.db-interface/store-changes definition exists."
        (is (callable? 'clouseau.db-interface/store-changes))))

;
; Tests for function behaviours
;

(deftest test-read-description-1
    (testing "clouseau.db-interface/read-description"
        ; use mock instead of jdbc/query
        (with-redefs [jdbc/query (fn [product package] product)]
            (let [product ["RHEL 6"                                   ; first
                              {:classname   "org.sqlite.JDBC"         ; second
                               :subprotocol "sqlite"
                               :subname     "packages/rhel6/primary.sqlite"
                           }]]
            (is (= (second product) (read-description product "package-name")))))))

(deftest test-read-description-2
    (testing "clouseau.db-interface/read-description"
        ; use mock instead of jdbc/query
        (with-redefs [jdbc/query (fn [product package] package)]
            (let [product ["RHEL 6"                                   ; first
                              {:classname   "org.sqlite.JDBC"         ; second
                               :subprotocol "sqlite"
                               :subname     "packages/rhel6/primary.sqlite"
                           }]]
            (is (= "select description from packages where lower(name)='package-name';"
                (read-description product "package-name")))
            (is (= "select description from packages where lower(name)='package-name';"
                (read-description product "Package-Name")))
            (is (= "select description from packages where lower(name)='package-name';"
                (read-description product "PACKAGE-NAME")))))))

