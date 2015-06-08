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

(ns clouseau.test-calendar
  (:require [clojure.test :refer :all]
            [clouseau.calendar :refer :all]))


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

(deftest test-get-calendar-existence
    "Check that the clouseau.calendar/get-calendar definition exists."
    (testing "if the clouseau.calendar/get-calendar definition exists."
        (is (callable? 'clouseau.calendar/get-calendar))))

(deftest test-format-date-using-desired-format-existence
    "Check that the clouseau.calendar/format-date-using-desired-format definition exists."
    (testing "if the clouseau.calendar/format-date-using-desired-format definition exists."
        (is (callable? 'clouseau.calendar/format-date-using-desired-format))))

(deftest test-format-date-time-existence
    "Check that the clouseau.calendar/format-date-time definition exists."
    (testing "if the clouseau.calendar/format-date-time definition exists."
        (is (callable? 'clouseau.calendar/format-date-time))))

;
; Tests for function behaviours
;

(deftest test-get-calendar
    "Check the function clouseau.calendar/get-calendar."
    (testing "the function clouseau.calendar/get-calendar." 
        (is (not (nil? (get-calendar))))
        (is (or
            (=  (class (get-calendar)) java.util.Calendar)
            (=  (class (get-calendar)) java.util.GregorianCalendar)))
        (is (or
            (=  (type  (get-calendar)) java.util.Calendar)
            (=  (type  (get-calendar)) java.util.GregorianCalendar)))
        (is (>  (.get (get-calendar) (java.util.Calendar/YEAR)) 2000))
        (is (>= (.get (get-calendar) (java.util.Calendar/MONTH)) 0))
        (is (<  (.get (get-calendar) (java.util.Calendar/MONTH)) 12))
        (is (>= (.get (get-calendar) (java.util.Calendar/DAY_OF_MONTH)) 0))
        (is (<= (.get (get-calendar) (java.util.Calendar/DAY_OF_MONTH)) 31)) ; let's be on the safe side
        (is (>= (.get (get-calendar) (java.util.Calendar/DAY_OF_YEAR)) 0))
        (is (<= (.get (get-calendar) (java.util.Calendar/DAY_OF_YEAR)) 366)) ; let's be on the safe side
        (is (>= (.get (get-calendar) (java.util.Calendar/WEEK_OF_YEAR)) 0))
        (is (<= (.get (get-calendar) (java.util.Calendar/WEEK_OF_YEAR)) 54)) ; let's be on the safe side
))

(deftest test-get-calendar-2
    "Check the function clouseau.calendar/get-calendar."
    (testing "the function clouseau.calendar/get-calendar." 
        (let [calendar (get-calendar)]
            (.set calendar 2000 01 01 10 20 30)
            (is (not (nil? calendar)))
            (is (or
                (=  (class calendar) java.util.Calendar)
                (=  (class calendar) java.util.GregorianCalendar)))
            (is (or
                (=  (type  calendar) java.util.Calendar)
                (=  (type  calendar) java.util.GregorianCalendar)))
            (is (=  (.get calendar (java.util.Calendar/YEAR)) 2000))
            (is (=  (.get calendar (java.util.Calendar/MONTH)) 1))
            (is (=  (.get calendar (java.util.Calendar/DAY_OF_MONTH)) 1)))
))

(deftest test-format-date-using-desired-format
    "Check the function clouseau.calendar/format-date-using-desired-format"
    (testing "the function clouseau.calendar/format-date-using-desired-format." 
        (let [calendar (get-calendar)]
            (.set calendar 2000 01 01 10 20 30)
)))

