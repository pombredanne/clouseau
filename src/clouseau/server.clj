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

(ns clouseau.server)

(require '[ring.util.response     :as http-response])

(require '[clouseau.products      :as products])
(require '[clouseau.calendar      :as calendar])
(require '[clouseau.html-renderer :as html-renderer])
(require '[clouseau.text-renderer :as text-renderer])
(require '[clouseau.db-interface  :as db-interface])

(defn println-and-flush
    "Original (println) has problem with syncing when it's called from more threads.
     This function is a bit better because it flushes all output immediatelly."
    [& more]
    (.write *out* (str (clojure.string/join " " more) "\n"))
    (flush))

(defn read-description
    "Read description from the database for specified product and package."
    [product package]
    (try
        (let [result (db-interface/read-description product package)
              desc   (:description (first result))]
            (if (not desc)
                ""  ; special value that will be handled later
                (.replaceAll desc "\n" "<br />")))
        (catch Exception e
            ; print error message in case of any DB-related exception
            (println-and-flush "read-description(): error accessing database '" (:subname (second product)) "'!")
            (println-and-flush e)
            (.printStackTrace e)
            (println "***********" package)
            nil)))  ; special value that will be handled later

(defn try-to-read-description
    "Read package description, but only when package is specified."
    [product package]
    (if package
        (read-description product package)
        ""))

(defn read-changes-statistic
    "Read number of changes made by all users."
    []
    (try
        (db-interface/read-changes-statistic)
        (catch Exception e
            ; print error message in case of any DB-related exception
            (println-and-flush "read-changes-statistic(): Error accessing database 'css_descriptions.db'!")
            (println-and-flush e)
            nil)))  ; special value that will be handled later

(defn read-changes
    "Read all changes made by all users."
    []
    (try
        (db-interface/read-changes)
        (catch Exception e
            ; print error message in case of any DB-related exception
            (println-and-flush "read-changes(): Error accessing database 'css_descriptions.db'!")
            (println-and-flush e)
            nil)))  ; special value that will be handled later

(defn read-changes-for-user
    "Read all changes made by specific user."
    [user-name]
    (try
        (db-interface/read-changes-for-user user-name)
        (catch Exception e
            ; print error message in case of any DB-related exception
            (println-and-flush "read-changes-for-user(): Error accessing database 'css_descriptions.db'!")
            (println-and-flush e)
            nil)))  ; special value that will be handled later

(defn read-package-descriptions
    [products package]
    (zipmap
        (for [product products]
            (first product))
        (for [product products]
            (try-to-read-description product package))))

(defn read-ccs-description
    [package]
    (try
        (let [result (db-interface/read-ccs-description package)
              desc   (:description (first result))]
            (if (not desc)
                ""     ; special value that will be handled later
                desc)) ; (.replaceAll desc "\n" "<br />"))))
        (catch Exception e
            (println-and-flush "read-ccs-description(): Error accessing database 'css_descriptions.db'!")
            (println-and-flush e)
            nil)))     ; special value that will be handled later

(defn try-to-read-ccs-description
    "Read package CCS description, but only when package is specified."
    [package]
    (if package
        (read-ccs-description package)
        ""))

(defn read-all-descriptions
    "Read all descriptions from a table 'ccs-db' stored in a file 'ccs_descriptions.db'."
    []
    (db-interface/read-all-descriptions))

(defn read-all-packages-provided-by-ccs
    "Read all packages from a table 'ccs-db' stored in a file 'ccs_descriptions.db'."
    []
    (db-interface/read-all-packages-provided-by-ccs))

(defn store-ccs-description
    "Store new ccs description into the table 'ccs-db' stored in a file 'ccs_descriptions.db'."
    [package description]
    (db-interface/store-ccs-description package description))

(defn not-empty-parameter?
    "Returns true if given parameter is not null and not empty at the same time."
    [parameter]
    (and parameter (not (empty? parameter))))

(defn store-changes
    "Store changes into the 'changes' table."
    [user-name package description]
    (if (and (not-empty-parameter? package) (not-empty-parameter? description))
        (let [date (calendar/format-date-time (calendar/get-calendar))]
            (db-interface/store-changes user-name package description date)
            (println-and-flush date user-name package)
        )))

(defn delete-package
    [package-name]
    (db-interface/delete-package-from-ccs-description package-name))

(defn trim-package-name
    [package-name]
    (db-interface/trim-package-in-ccs-description package-name))

(defn lowercase-package-name
    [package-name]
    (db-interface/lowercase-package-in-ccs-description package-name))

(defn log-request-information
    [request]
    (println-and-flush "time:        " (.toString (new java.util.Date)))
    (println-and-flush "addr:        " (request :remote-addr))
    (println-and-flush "params:      " (request :params))
    (println-and-flush "user-agent:  " ((request :headers) "user-agent"))
    (println-and-flush ""))

(defn get-products-without-descriptions
    [products package-descriptions]
   ; (sort
        (for [product products
            ; all descriptions that are equal to "" are not specified
            :when (empty? (get package-descriptions (first product)))]
            (first product)));)

(defn get-products-with-descriptions
    [products package-descriptions]
    (sort
        (for [product products
            ; all descriptions that are not equal to "" are specified
            :when (not (empty? (get package-descriptions (first product))))]
            (first product))))

(defn get-products-per-description
    [package-descriptions products]
    (let [variants
        (into #{}
            (for [product products]
                (get package-descriptions product)))]
        (for [variant variants]
            [variant (for [product products :when (= variant (get package-descriptions product))] product)])))

(defn get-user-name
    [new-user-name old-user-name]
    (or new-user-name old-user-name))

(defn generate-normal-response
    "Generate server response in HTML format."
    [products package package-descriptions ccs-description
     products-per-description products-without-descriptions new-description user-name]
     (let [html-output (html-renderer/render-front-page products package package-descriptions ccs-description products-per-description products-without-descriptions new-description user-name)]
        (store-changes user-name package new-description)
        (if user-name
            (-> (http-response/response html-output)
                (http-response/set-cookie :user-name user-name {:max-age 36000000})
                (http-response/content-type "text/html"))
            (-> (http-response/response html-output)
                (http-response/content-type "text/html")))))

(defn generate-txt-normal-response
    "Generate server response in text format."
    [products package package-descriptions ccs-description
     products-per-description products-without-descriptions new-description user-name]
     (let [text-output (text-renderer/render-front-page products package package-descriptions ccs-description products-per-description products-without-descriptions new-description user-name)]
        (store-changes user-name package new-description)
            (-> (http-response/response text-output)
                (http-response/content-type "text/plain"))))

(defn generate-error-response
    "Generate error message in HTML format in case any error occured in Clouseau."
    [package user-name message]
    (-> (http-response/response (html-renderer/render-error-page package user-name message))
        (http-response/content-type "text/html")))

(defn generate-txt-error-response
    "Generate error message in text/plain format in case any error occured in Clouseau."
    [package user-name message]
    (-> (http-response/response (str "Sorry, error occured in Clouseau:" message))
        (http-response/content-type "text/plain")))

(defn package-error
    "Returns sequence of products for whom the database can't be read
     (it's easy to spot this error because package-descriptions map contains
      nil for such packages."
    [package-descriptions]
    (keys (filter #(nil? (val %)) package-descriptions)))

(defn generate-error-message-package-db-error
    "Generate error message that is thrown in case of any error during working with database."
    [package-descriptions]
    (let [error-products (package-error package-descriptions)]
        (str "Can not access following " (count error-products) " database" (if (> (count error-products) 1) "s" "") ": " (clojure.string/join ", " error-products))))

(defn generate-response
    [package new-description output-format new-user-name old-user-name]
    (let [ccs-description               (try-to-read-ccs-description package)
          package-descriptions          (read-package-descriptions products/products package)
          products-without-descriptions (get-products-without-descriptions products/products package-descriptions)
          products-with-descriptions    (get-products-with-descriptions products/products package-descriptions)
          products-per-description      (get-products-per-description package-descriptions products-with-descriptions)
          user-name                     (get-user-name new-user-name old-user-name)]
          (condp = output-format
              "html" (cond (not ccs-description)                (generate-error-response package user-name "Can not read from the database file 'ccs_descriptions.db'!")
                           (package-error package-descriptions) (generate-error-response package user-name (generate-error-message-package-db-error package-descriptions))
                           :else                                (generate-normal-response products/products package package-descriptions ccs-description products-per-description products-without-descriptions new-description user-name))
              "txt"  (cond (not ccs-description)                (generate-txt-error-response package user-name "Can not read from the database file 'ccs_descriptions.db'!")
                           (package-error package-descriptions) (generate-txt-error-response package user-name (generate-error-message-package-db-error package-descriptions))
                           :else                                (generate-txt-normal-response products/products package package-descriptions ccs-description products-per-description products-without-descriptions new-description user-name))
)))

(defn process
    "Gather all required informations and send it back to user."
    [package new-description output-format new-user-name old-user-name]
    (let [trimmed-package-name (if package (clojure.string/trim package) nil)]
        (if (and (not-empty-parameter? package) (not-empty-parameter? new-description))
            (try
                ; we need to use lowercase package name to avoid issue #66
                (store-ccs-description (clojure.string/lower-case trimmed-package-name) new-description)
                (generate-response trimmed-package-name new-description output-format new-user-name old-user-name)
                (catch Exception e
                    (println "Error writing into database 'ccs_descriptions.db':" e)
                    (generate-error-response trimmed-package-name old-user-name (str "Can not write into database file 'ccs_descriptions.db': " e))))
            (generate-response trimmed-package-name new-description output-format new-user-name old-user-name))))

(defn perform-normal-processing
    "Generates Clouseau main page."
    [request]
    (log-request-information request)
    (let [params              (request :params)
          cookies             (request :cookies)
          output-format       (get params "format" "html")
          package             (get params "package")
          new-description     (get params "new-description")
          new-user-name       (get params "user-name")
          old-user-name       (get (get cookies "user-name") :value)]
          (println-and-flush "Incoming cookies: " cookies)
          (let [response (process package new-description output-format new-user-name old-user-name)]
              (println-and-flush "Outgoing cookies: " (get response :cookies))
              response
          )))

(defn render-all-descriptions
    "Create page containing all descriptions made by CCS users in Clouseau database."
    [request]
    (let [descriptions (read-all-descriptions)
          user-name    (get (get (request :cookies) "user-name") :value)]
        ;(println-and-flush descriptions)
        (-> (http-response/response (html-renderer/render-descriptions descriptions user-name))
            (http-response/content-type "text/html"))))

(defn render-users-info
    "Create page containing user info(s) for all users."
    [request]
    (let [changes-statistic (read-changes-statistic)
          changes           (read-changes)
          user-name         (get (get (request :cookies) "user-name") :value)]
        (-> (http-response/response (html-renderer/render-users changes-statistic changes user-name))
            (http-response/content-type "text/html"))))

(defn render-user-info
    "Create page containing detailed informations about selected user."
    [request]
    (let [params        (request :params)
          user-name     (get (get (request :cookies) "user-name") :value)
          selected-user (get params "name")
          changes       (read-changes-for-user selected-user)]
          (println "User name: " selected-user)
          ;(println "User made changes: " changes)
        (-> (http-response/response (html-renderer/render-user changes selected-user user-name))
            (http-response/content-type "text/html"))))

(defn render-admin-interface
    "Created admin interface page."
    [request operation]
    (let [params           (request :params)
          user-name        (get (get (request :cookies) "user-name") :value)
          selected-package (get params "package")]
          (println "Selected package " selected-package)
          (println "Operation        " operation)
          (case operation
                :delete    (delete-package    selected-package)
                :trim      (trim-package-name selected-package)
                :lowercase (lowercase-package-name selected-package)
                :none      nil)
          (let [packages    (read-all-packages-provided-by-ccs)]
              (println "Packages         " (count packages))
              (-> (http-response/response (html-renderer/render-admin-interface packages user-name))
                  (http-response/content-type "text/html")))))

(defn return-file
    "Creates HTTP response containing content of specified file.
     Special value nil / HTTP response 404 is returned in case of any I/O error."
    [file-name content-type]
    (let [file (new java.io.File "www" file-name)]
        (println-and-flush "Returning file " (.getAbsolutePath file))
        (if (.exists file)
            (-> (http-response/response file)
                (http-response/content-type content-type))
            (println-and-flush "return-file(): can not access file: " (.getName file)))))

(defn handler
    "Handler that is called by Ring for all requests received from user(s)."
    [request]
    (println-and-flush "request URI: " (request :uri))
    (let [uri (request :uri)]
        (condp = uri
            "/favicon.ico"       (return-file "favicon.ico" "image/x-icon")
            "/bootstrap.min.css" (return-file "bootstrap.min.css" "text/css")
            "/smearch.css"       (return-file "smearch.css" "text/css")
            "/bootstrap.min.js"  (return-file "bootstrap.min.js" "application/javascript")
            "/"                  (perform-normal-processing request)
            "/descriptions"      (render-all-descriptions request)
            "/users"             (render-users-info request)
            "/user"              (render-user-info request)
            "/admin"             (render-admin-interface request :none)
            "/delete"            (render-admin-interface request :delete)
            "/trim"              (render-admin-interface request :trim)
            "/lowercase"         (render-admin-interface request :lowercase)
            )))

