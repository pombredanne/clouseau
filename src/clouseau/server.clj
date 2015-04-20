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
(require '[hiccup.core :as hiccup])
(require '[hiccup.page :as page])
(require '[hiccup.form :as form])

(require '[clojure.java.jdbc :as jdbc])

(require '[clouseau.products :as products])

;
; This database file contains table named 'ccs-description'.
;
; Table 'ccs-description' should have the following format:
;    create table packages (
;        name        text not null,
;        description text not null
;    );
;
(def ccs-db
    {:classname   "org.sqlite.JDBC"
     :subprotocol "sqlite"
     :subname     "ccs_descriptions.db"
    })

;
; This database file contains table named 'changes'.
;
; Table 'changes' should have the following format:
;     create table changes (
;         id          integer primary key asc,
;         date_time   text,
;         user_name   text,
;         package     text,
;         description text
;     );
;
(def changes-db
    {:classname   "org.sqlite.JDBC"
     :subprotocol "sqlite"
     :subname     "changes.db"
    })

(defn println-and-flush
    "Original (println) has problem with syncing when it's called from more threads.
     This function is a bit better because it flushes all output immediatelly."
    [& more]
    (.write *out* (str (clojure.string/join " " more) "\n"))
    (flush))

(defn get-calendar
    "Gets a calendar using the default time zone and default locale."
    []
    (java.util.Calendar/getInstance))

(defn format-date-using-desired-format
    "Format given date using desired format, for example 'yyyy-MM-dd' etc."
    [calendar desired-format]
    (let [date-format (new java.text.SimpleDateFormat desired-format)]
        (.format date-format (.getTime calendar))))

(defn format-date-time
    "Format given date using the following format: 'yyyy-MM-dd HH:mm:ss'"
    [calendar]
    (format-date-using-desired-format calendar "yyyy-MM-dd HH:mm:ss"))

(defn read-description
    "Read description from the database for specified product and package."
    [product package]
    (try
        (let [result (jdbc/query (second product) (str "select description from packages where name='" package "';"))
              desc   (:description (first result))]
            (if (not desc)
                ""  ; special value that will be handled later
                (.replaceAll desc "\n" "<br />")))
        (catch Exception e
            (println-and-flush "read-description(): error accessing database '" (:subname (second product)) "'!")
            nil)))  ; special value that will be handled later

(defn read-changes-statistic
    "Read number of changes made by all users."
    []
    (try
        (jdbc/query changes-db (str "select user_name, count(*) as cnt from changes group by user_name order by cnt desc;"))
        (catch Exception e
            (println-and-flush "read-ccs-description(): Error accessing database 'css_descriptions.db'!")
            (println e)
            nil)))  ; special value that will be handled later

(defn read-changes
    "Read all changes made by all users."
    []
    (try
        (jdbc/query changes-db (str "select * from changes order by id;"))
        (catch Exception e
            (println-and-flush "read-ccs-description(): Error accessing database 'css_descriptions.db'!")
            (println e)
            nil)))  ; special value that will be handled later

(defn read-changes-for-user
    "Read all changes made by specific user."
    [user-name]
    (try
        (jdbc/query changes-db [(str "select * from changes where user_name=? order by id;") user-name])
        (catch Exception e
            (println-and-flush "read-ccs-description(): Error accessing database 'css_descriptions.db'!")
            (println e)
            nil)))  ; special value that will be handled later

(defn read-package-descriptions
    [products package]
    (zipmap
        (for [product products]
            (first product))
        (for [product products]
            (read-description product package))))

(defn read-ccs-description
    [package]
    (try
        (let [result (jdbc/query ccs-db (str "select description from packages where name='" package "';"))
              desc   (:description (first result))]
            (if (not desc)
                ""     ; special value that will be handled later
                desc)) ; (.replaceAll desc "\n" "<br />"))))
        (catch Exception e
            (println-and-flush "read-ccs-description(): Error accessing database 'css_descriptions.db'!")
            nil)))     ; special value that will be handled later

(defn read-all-descriptions
    "Read all descriptions from a table 'ccs-db' stored in a file 'ccs_descriptions.db'."
    []
    ; we need to use trim() here because some package names starts with one space or even with more spaces
    ; due to errors in the original database
    (jdbc/query ccs-db (str "select trim(name) as name, description from packages order by trim(name);")))

(defn store-ccs-description
    "Store new ccs description into the table 'ccs-db' stored in a file 'ccs_descriptions.db'."
    [package description]
    (jdbc/delete! ccs-db :packages ["name = ?" package])
    (jdbc/insert! ccs-db :packages {:name package :description description}))

(defn not-empty-parameter?
    [parameter]
    (and parameter (not (empty? parameter))))

(defn store-changes
    [user-name package description]
    (if (and (not-empty-parameter? package) (not-empty-parameter? description))
        (let [date (format-date-time (get-calendar))]
            (jdbc/insert! changes-db :changes {:date_time date :user_name user-name :package package :description description})
            (println-and-flush date user-name package)
        )))

(defn render-html-header
    [package]
    [:head
        [:title "Clouseau   " package]
        [:meta {:name "Author"    :content "Pavel Tisnovsky"}]
        [:meta {:name "Generator" :content "Clojure"}]
        [:meta {:http-equiv "Content-type" :content "text/html; charset=utf-8"}]
        ;(page/include-css "http://torment.usersys.redhat.com/openjdk/style.css")]
        (page/include-css "bootstrap.min.css")
        (page/include-css "smearch.css")
        (page/include-js  "bootstrap.min.js")
    ] ; head
)

(defn render-footer
    []
    [:div "<br /><br /><br /><br />Author: Pavel Tisnovsky &lt;<a href='mailto:ptisnovs@redhat.com'>ptisnovs@redhat.com</a>&gt;&nbsp;&nbsp;&nbsp;"
          "<a href='https://mojo.redhat.com/message/955597'>RFE and general discussion about Clouseau in Mojo</a><br />"])

(defn render-search-field
    [package]
    (form/form-to {:class "navbar-form navbar-left" :role "search"} [:get "/" ]
        [:div {:class "input-group"}
            [:span {:class "input-group-addon"} "Package"]
            (form/text-field {:size "40" :class "form-control" :placeholder "Examples: 'bash', 'vim-enhanced', 'kernel', 'gnome-desktop'"} "package" (str package))
            [:div {:class "input-group-btn"}
                (form/submit-button {:class "btn btn-default"} "Search")]]))

(defn render-name-field
    [user-name]
    (form/form-to {:class "navbar-form navbar-left" :role "search"} [:get "/" ]
        [:div {:class "input-group"}
            ;[:span {:class "input-group-addon"} "Name"]
            (form/text-field {:size "10" :class "form-control" :placeholder "User name"} "user-name" (str user-name))
            [:div {:class "input-group-btn"}
                (form/submit-button {:class "btn btn-default"} "Remember me")]]))

(defn render-navigation-bar-section
    [package user-name]
    [:nav {:class "navbar navbar-inverse navbar-fixed-top" :role "navigation"}
        [:div {:class "container-fluid"}
            [:div {:class "row"}
                [:div {:class "col-md-2"}
                    [:div {:class "navbar-header"}
                        [:a {:href "/" :class "navbar-brand"} "Clouseau"]
                    ] ; ./navbar-header
                ] ; col ends
                [:div {:class "col-md-4"}
                    (render-search-field package)
                ] ; col ends
                [:div {:class "col-md-2"}
                    [:div {:class "navbar-header"}
                        [:a {:href "/descriptions" :class "navbar-brand"} "All CCS descriptions"]
                    ] ; ./navbar-header
                ] ; col ends
                [:div {:class "col-md-3"}
                    (render-name-field user-name)
                ]
                [:div {:class "col-md-1"}
                    [:div {:class "navbar-header"}
                        [:a {:href "/users" :class "navbar-brand"} "Users"]
                    ] ; ./navbar-header
                ] ; col ends
            ] ; row ends
        ] ; /.container-fluid
]); </nav>

(defn render-description
    [description]
    (if-not description
        [:div {:class "alert alert-danger"} "Not found"]
        [:div {:class "alert alert-success"} description]))

(defn render-error-page
    "Render error page with a 'back' button."
    [package user-name message]
    (page/xhtml
        (render-html-header "")
        [:body
            [:div {:class "container"}
                (render-navigation-bar-section package user-name)
                [:div {:class "col-md-10"}
                    [:h2 "Sorry, error occured in Clouseau"]
                    [:p message]
                    [:button {:class "btn btn-primary" :onclick "window.history.back()" :type "button"} "Back"]
                ]
                [:br][:br][:br][:br]
                (render-footer)
            ] ; </div class="container">
        ] ; </body>
    ))

(defn package?
    [package]
    (and package (not (empty? package))))

(defn show-info-about-update
    [package]
    [:div
        [:div {:class "label label-warning"} (str "Description for the package '" package "' has been updated, thank you!")]
        [:br]
        [:br]])

(defn text-renderer
    [products package package-descriptions ccs-description products-per-descriptions products-without-descriptions new-description user-name]
    (str
        "[Package]\n" package
        "\n\n[CCS Description]\n" ccs-description
        "\n\n"
        (apply str
            (for [p package-descriptions]
                (str "[" (key p) "]\n"
                         (val p) "\n\n")
            ))))

(defn html-renderer
    [products package package-descriptions ccs-description products-per-descriptions products-without-descriptions new-description user-name]
    (page/xhtml
        (render-html-header package)
        [:body
            [:div {:class "container"}
                (render-navigation-bar-section package user-name)

                (if new-description
                    (show-info-about-update package))

                (if (package? package)
                    (form/form-to [:post "/"]
                        [:div {:class "label label-primary"} "Description provided by CCS"]
                        [:br]
                        (form/hidden-field "package" (str package))
                        (form/text-area {:cols "120" :rows "10"} "new-description" ccs-description)
                        [:br]
                        (form/submit-button {:class "btn btn-danger"} "Update description")
                        [:br]
                        [:br]
                    ))

                (if (package? package)
                    (for [products-per-description products-per-descriptions]
                        [:div
                            (for [product (second products-per-description)]
                                [:div {:class "label label-primary" :style "margin-right:3px"} product]) 
                            (render-description (first products-per-description))]
                    ))

                (if (and (package? package) products-without-descriptions)
                    [:div
                    (for [product products-without-descriptions]
                        [:div {:class "label label-primary" :style "margin-right:3px"} product])
                        [:div {:class "alert alert-danger"} "Not found"]]
                    )

                (render-footer)
            ] ; </div class="container">
        ] ; </body>
    ))

(defn html-renderer-descriptions
    [descriptions user-name]
    (page/xhtml
        (render-html-header nil)
        [:body
            [:div {:class "container"}
                (render-navigation-bar-section nil user-name)
                    (for [description descriptions]
                        [:div [:div {:class "label label-warning"} [:a {:href (str "../?package=" (get description :name) ) } (get description :name)]]
                              [:div {:class "alert alert-success"} (.replaceAll (str "" (get description :description)) "\\n" "<br />")]]
                    )
                (render-footer)
            ] ; </div class="container">
        ] ; </body>
    ))

(defn html-renderer-user
    [changes selected-user user-name]
    (page/xhtml
        (render-html-header nil)
        [:body
            [:div {:class "container"}
                (render-navigation-bar-section nil user-name)
                [:h2 "Changes made by" selected-user]
                [:table {:class "table table-stripped table-hover" :style "width:auto"}
                    [:tr [:th "ID"]
                         [:th "Date"]
                         [:th "Package"]
                         [:th "Description"]]
                    (for [change changes]
                        [:tr [:td (:id change)]
                             [:td (:date_time change)]
                             [:td [:a {:href (str "/?package="  (:package change))} (:package change)]]
                             [:td (:description change)]])
                ]
                (render-footer)
            ] ; </div class="container">
        ] ; </body>
    ))

(defn html-renderer-users
    [statistic changes user-name]
    (page/xhtml
        (render-html-header nil)
        [:body
            [:div {:class "container"}
                (render-navigation-bar-section nil user-name)
                [:table {:class "table table-stripped table-hover" :style "width:auto"}
                    [:tr [:th "User name"]
                         [:th "Changes made"]]
                    (for [stat statistic]
                        [:tr [:td [:a {:href (str "user?name=" (:user_name stat))} (:user_name stat)]]
                             [:td (:cnt stat)]]
                    )
                ]
                [:br]
                [:table {:class "table table-stripped table-hover" :style "width:auto"}
                    [:tr [:th "ID"]
                         [:th "Date"]
                         [:th "Package"]
                         [:th "User"]
                         [:th "Description"]]
                    (for [change changes]
                        [:tr [:td (:id change)]
                             [:td (:date_time change)]
                             [:td [:a {:href (str "/?package="  (:package change))} (:package change)]]
                             [:td [:a {:href (str "user?name=" (:user_name change))} (:user_name change)]]
                             [:td (:description change)]])
                ]
                (render-footer)
            ] ; </div class="container">
        ] ; </body>
    ))

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
     (let [html-output (html-renderer products package package-descriptions ccs-description products-per-description products-without-descriptions new-description user-name)]
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
     (let [text-output (text-renderer products package package-descriptions ccs-description products-per-description products-without-descriptions new-description user-name)]
        (store-changes user-name package new-description)
            (-> (http-response/response text-output)
                (http-response/content-type "text/plain"))))

(defn generate-error-response
    "Generate error message in HTML format in case any error occured in Clouseau."
    [package user-name message]
    (-> (http-response/response (render-error-page package user-name message))
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
    (let [ccs-description               (read-ccs-description package)
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
    (if (and (not-empty-parameter? package) (not-empty-parameter? new-description))
        (try
            (store-ccs-description package new-description)
            (generate-response package new-description output-format new-user-name old-user-name)
            (catch Exception e
                (println "Error writing into database 'ccs_descriptions.db':" e)
                (generate-error-response package old-user-name (str "Can not write into database file 'ccs_descriptions.db': " e))))
        (generate-response package new-description output-format new-user-name old-user-name)))

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
        (-> (http-response/response (html-renderer-descriptions descriptions user-name))
            (http-response/content-type "text/html"))))

(defn render-users-info
    "Create page containing user info(s) for all users."
    [request]
    (let [changes-statistic (read-changes-statistic)
          changes           (read-changes)
          user-name         (get (get (request :cookies) "user-name") :value)]
        (-> (http-response/response (html-renderer-users changes-statistic changes user-name))
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
        (-> (http-response/response (html-renderer-user changes selected-user user-name))
            (http-response/content-type "text/html"))))

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
            "/user"              (render-user-info request))))

