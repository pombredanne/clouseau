;;;
;;;   Clouseau
;;; 
;;;    Copyright (C) 2015, 2016 Pavel Tisnovsky <ptisnovs@redhat.com>
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

(ns clouseau.test-html-renderer
  (:require [clojure.test :refer :all]
            [clouseau.html-renderer :refer :all]))

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

(deftest test-render-html-header-existence
    "Check that the clouseau.html-renderer/render-html-header definition exists."
    (testing "if the clouseau.html-renderer/render-html-header definition exists."
        (is (callable? 'clouseau.html-renderer/render-html-header))))

(deftest test-render-html-footer-existence
    "Check that the clouseau.html-renderer/render-html-footer definition exists."
    (testing "if the clouseau.html-renderer/render-html-footer definition exists."
        (is (callable? 'clouseau.html-renderer/render-html-footer))))

(deftest test-render-search-field-existence
    "Check that the clouseau.html-renderer/render-search-field definition exists."
    (testing "if the clouseau.html-renderer/render-search-field definition exists."
        (is (callable? 'clouseau.html-renderer/render-search-field))))

(deftest test-render-name-field-existence
    "Check that the clouseau.html-renderer/render-name-field definition exists."
    (testing "if the clouseau.html-renderer/render-name-field definition exists."
        (is (callable? 'clouseau.html-renderer/render-name-field))))

(deftest test-render-navigation-bar-section-existence
    "Check that the clouseau.html-renderer/render-navigation-bar-section definition exists."
    (testing "if the clouseau.html-renderer/render-navigation-bar-section definition exists."
        (is (callable? 'clouseau.html-renderer/render-navigation-bar-section))))


(deftest test-render-description-existence
    "Check that the clouseau.html-renderer/render-description definition exists."
    (testing "if the clouseau.html-renderer/render-description definition exists."
        (is (callable? 'clouseau.html-renderer/render-description))))

(deftest test-render-error-page-existence
    "Check that the clouseau.html-renderer/render-error-page definition exists."
    (testing "if the clouseau.html-renderer/render-error-page definition exists."
        (is (callable? 'clouseau.html-renderer/render-error-page))))

(deftest test-render-front-page-existence
    "Check that the clouseau.html-renderer/render-front-page definition exists."
    (testing "if the clouseau.html-renderer/render-front-page definition exists."
        (is (callable? 'clouseau.html-renderer/render-front-page))))

(deftest test-render-descriptions-existence
    "Check that the clouseau.html-renderer/render-descriptions definition exists."
    (testing "if the clouseau.html-renderer/render-descriptions definition exists."
        (is (callable? 'clouseau.html-renderer/render-descriptions))))

(deftest test-render-navigation-bar-section-existence
    "Check that the clouseau.html-renderer/render-navigation-bar-section definition exists."
    (testing "if the clouseau.html-renderer/render-navigation-bar-section definition exists."
        (is (callable? 'clouseau.html-renderer/render-navigation-bar-section))))

