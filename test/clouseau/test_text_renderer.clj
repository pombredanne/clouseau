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

(ns clouseau.test-text-renderer
  (:require [clojure.test :refer :all]
            [clouseau.text-renderer :refer :all]))

;
; Common functions used by tests.
;

(defn callable?
    "Test if given function-name is bound to the real function."
    [function-name]
    (clojure.test/function? function-name))

;
; Tests for existence of various functions
;

(deftest test-render-front-page-existence
    "Check that the clouseau.text-renderer/render-front-page definition exists."
    (testing "if the clouseau.text-renderer/render-front-page definition exists."
        (is (callable? 'clouseau.text-renderer/render-front-page))))

(deftest test-render-package-descriptions-existence
    "Check that the clouseau.text-renderer/render-package-descriptions definition exists."
    (testing "if the clouseau.text-renderer/render-package-descriptions definition exists."
        (is (callable? 'clouseau.text-renderer/render-package-descriptions))))

;
; Tests for function behaviours
;

(deftest test-render-front-page-package-parameter
    "Check the function clouseau.text-renderer/render-front-page"
    (testing "the function clouseau.text-renderer/render-front-page" 
        (is (= (render-front-page nil nil nil nil nil nil nil nil) "[Package]\n\n\n[CCS Description]\n\n\n"))
        (is (= (render-front-page nil "PACKAGE" nil nil nil nil nil nil) "[Package]\nPACKAGE\n\n[CCS Description]\n\n\n"))
        (is (= (render-front-page nil "" nil nil nil nil nil nil) "[Package]\n\n\n[CCS Description]\n\n\n"))
        (is (= (render-front-page nil "*" nil nil nil nil nil nil) "[Package]\n*\n\n[CCS Description]\n\n\n"))))

(deftest test-render-front-page-ccs-description-parameter
    "Check the function clouseau.text-renderer/render-front-page"
    (testing "the function clouseau.text-renderer/render-front-page" 
        (is (= (render-front-page nil "PACKAGE" nil nil nil nil nil nil) "[Package]\nPACKAGE\n\n[CCS Description]\n\n\n"))
        (is (= (render-front-page nil "PACKAGE" nil "" nil nil nil nil) "[Package]\nPACKAGE\n\n[CCS Description]\n\n\n"))
        (is (= (render-front-page nil "PACKAGE" nil "***" nil nil nil nil) "[Package]\nPACKAGE\n\n[CCS Description]\n***\n\n"))))

(deftest test-render-front-page-package-descriptions-parameter
    "Check the function clouseau.text-renderer/render-front-page"
    (testing "the function clouseau.text-renderer/render-front-page" 
        (is (= (render-front-page nil "PACKAGE" nil "***" nil nil nil nil) "[Package]\nPACKAGE\n\n[CCS Description]\n***\n\n"))
        (is (= (render-front-page nil "PACKAGE" {"x" "y"} "***" nil nil nil nil) "[Package]\nPACKAGE\n\n[CCS Description]\n***\n\n[x]\ny\n\n"))
        (is (= (render-front-page nil "PACKAGE" {"prod1" "text1" "prod2" "text2"} "***" nil nil nil nil) "[Package]\nPACKAGE\n\n[CCS Description]\n***\n\n[prod1]\ntext1\n\n[prod2]\ntext2\n\n"))))

(deftest test-render-package-descriptions
    "Check the function clouseau.text-renderer/render-package-descriptions"
    (testing "the function clouseau.text-renderer/render-package-descriptions"
        (is (= (render-package-descriptions {"x" "y"}) '("[x]\ny\n\n")))
        (is (= (render-package-descriptions {"package1" "description1"}) '("[package1]\ndescription1\n\n")))
        (is (= (render-package-descriptions {"package1" "description1"
                                             "package2" "description2"})
                                             '("[package1]\ndescription1\n\n"
                                               "[package2]\ndescription2\n\n")))
        (is (= (render-package-descriptions {"package1" "description1"
                                             "package2" "description2"
                                             "package3" "description3"})
                                             '("[package1]\ndescription1\n\n"
                                               "[package2]\ndescription2\n\n"
                                               "[package3]\ndescription3\n\n")))
        (is (= (render-package-descriptions {"package1" "description1"
                                             "package2" "description2"
                                             "package3" "description3"
                                             "package4" "description4"})
                                             '("[package1]\ndescription1\n\n"
                                               "[package2]\ndescription2\n\n"
                                               "[package3]\ndescription3\n\n"
                                               "[package4]\ndescription4\n\n")))))

