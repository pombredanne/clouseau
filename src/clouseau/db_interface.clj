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

(ns clouseau.db-interface)

(require '[clojure.java.jdbc      :as jdbc])
(require '[clouseau.db-spec       :as db-spec])

(defn read-description
    [product package]
    (jdbc/query (second product) (str "select description from packages where lower(name)='" (clojure.string/lower-case package) "';")))

(defn read-changes-statistic
    []
    (jdbc/query db-spec/changes-db (str "select user_name, count(*) as cnt from changes group by user_name order by cnt desc;")))

(defn read-changes
    []
    (jdbc/query db-spec/changes-db (str "select * from changes order by id;")))

(defn read-changes-for-user
    [user-name]
    (jdbc/query db-spec/changes-db [(str "select * from changes where user_name=? order by id;") user-name]))

(defn read-ccs-description
    [package]
    (jdbc/query db-spec/ccs-db (str "select description from packages where lower(name)='" (clojure.string/lower-case package) "';")))

(defn read-all-descriptions
    []
    ; we need to use trim() here because some package names starts with one space or even with more spaces
    ; due to errors in the original database
    (jdbc/query db-spec/ccs-db (str "select trim(name) as name, description from packages order by trim(name);")))

(defn read-all-packages-provided-by-ccs
    []
    (jdbc/query db-spec/ccs-db (str "select name, description from packages order by name;")))

(defn store-ccs-description
    [package description]
    (jdbc/delete! db-spec/ccs-db :packages ["name = ?" package])
    (jdbc/insert! db-spec/ccs-db :packages {:name package :description description}))

(defn store-changes
    [user-name package description date]
    (jdbc/insert! db-spec/changes-db :changes {:date_time date :user_name user-name :package package :description description}))

(defn delete-package-from-ccs-description
    [package]
    (jdbc/delete! db-spec/ccs-db :packages ["name = ?" package]))

(defn trim-package-in-ccs-description
    [package]
    (jdbc/update! db-spec/ccs-db :packages {:name (clojure.string/trim package)} ["name=?" package]))

