(ns clouseau.server)

(require '[ring.util.response     :as http-response])
(require '[hiccup.core :as hiccup])
(require '[hiccup.page :as page])
(require '[hiccup.form :as form])

(require '[clojure.java.jdbc :as jdbc])

(require '[clouseau.products :as products])

(def ccs-db
    {:classname   "org.sqlite.JDBC"
     :subprotocol "sqlite"
     :subname     "ccs_descriptions.db"
    })

(defn read-description
    [product package]
    (let [result (jdbc/query (second product) (str "select description from packages where name='" package "';"))
          desc   (:description (first result))]
        (if (not desc)
            nil
            (.replaceAll desc "\n" "<br />"))))

(defn read-package-descriptions
    [products package]
    (zipmap
        (for [product products]
            (first product))
        (for [product products]
            (read-description product package))))

(defn read-ccs-description
    [package]
    (let [result (jdbc/query ccs-db (str "select description from packages where name='" package "';"))
          desc   (:description (first result))]
        (if (not desc)
            nil
            desc))) ; (.replaceAll desc "\n" "<br />"))))

(defn read-all-descriptions
    []
    (jdbc/query ccs-db (str "select name, description from packages order by name;")))

(defn store-ccs-description
    [package description]
    (jdbc/delete! ccs-db :packages ["name = ?" package])
    (jdbc/insert! ccs-db :packages {:name package :description description})
)

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

(defn render-navigation-bar-section
    [package]
    [:nav {:class "navbar navbar-inverse navbar-fixed-top" :role "navigation"}
        [:div {:class "container-fluid"}
            [:div {:class "row"}
                [:div {:class "col-md-2"}
                    [:div {:class "navbar-header"}
                        [:a {:href "/" :class "navbar-brand"} "Clouseau"]
                    ] ; ./navbar-header
                ] ; col ends
                [:div {:class "col-md-5"}
                    (render-search-field package)
                ] ; col ends
                [:div {:class "col-md-2"}
                    [:div {:class "navbar-header"}
                        [:a {:href "/descriptions" :class "navbar-brand"} "All CCS descriptions"]
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

(defn package?
    [package]
    (and package (not (empty? package))))

(defn show-info-about-update
    [package]
    [:div
        [:div {:class "label label-warning"} (str "Description for the package '" package "' has been updated, thank you!")]
        [:br]
        [:br]])

(defn html-renderer
    [products package package-descriptions ccs-description products-per-descriptions products-without-descriptions new-description]
    (page/xhtml
        (render-html-header package)
        [:body
            [:div {:class "container"}
                (render-navigation-bar-section package)

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
    [descriptions]
    (page/xhtml
        (render-html-header nil)
        [:body
            [:div {:class "container"}
                (render-navigation-bar-section nil)
                    (for [description descriptions]
                        [:div [:div {:class "label label-warning"} [:a {:href (str "../?package=" (get description :name) ) } (get description :name)]]
                              [:div {:class "alert alert-success"} (.replaceAll (str "" (get description :description)) "\\n" "<br />")]]
                    )
                (render-footer)
            ] ; </div class="container">
        ] ; </body>
    ))

(defn not-empty-parameter?
    [parameter]
    (and parameter (not (empty? parameter))))

(defn log-request-information
    [request]
    (println "time:        " (.toString (new java.util.Date)))
    (println "addr:        " (request :remote-addr))
    (println "params:      " (request :params))
    (println "user-agent:  " ((request :headers) "user-agent"))
    (println ""))

(defn get-products-without-descriptions
    [products package-descriptions]
   ; (sort
        (for [product products :when (not (get package-descriptions (first product)))]
            (first product)));)

(defn get-products-with-descriptions
    [products package-descriptions]
    (sort
        (for [product products :when (get package-descriptions (first product))]
            (first product))))

(defn get-products-per-description
    [package-descriptions products]
    (let [variants
        (into #{}
            (for [product products]
                (get package-descriptions product)))]
        (for [variant variants]
            [variant (for [product products :when (= variant (get package-descriptions product))] product)])))

(defn process
    [package new-description]
    (if (and (not-empty-parameter? package) (not-empty-parameter? new-description))
        (store-ccs-description package new-description))
    (let [ccs-description               (read-ccs-description package)
          package-descriptions          (read-package-descriptions products/products package)
          products-without-descriptions (get-products-without-descriptions products/products package-descriptions)
          products-with-descriptions    (get-products-with-descriptions products/products package-descriptions)
          products-per-description      (get-products-per-description package-descriptions products-with-descriptions)]
        (-> (http-response/response (html-renderer products/products package package-descriptions ccs-description products-per-description products-without-descriptions new-description))
            (http-response/content-type "text/html"))))

(defn perform-normal-processing
    [request]
    (log-request-information request)
    (let [params              (request :params)
          package             (get params "package")
          new-description     (get params "new-description")]
          (process package new-description)))

(defn render-all-descriptions
    [request]
    (let [descriptions (read-all-descriptions)]
        (println descriptions)
        (-> (http-response/response (html-renderer-descriptions descriptions))
            (http-response/content-type "text/html"))))

(defn return-file
    [file-name content-type]
    (let [file (new java.io.File "www" file-name)]
        (println "Returning file " (.getAbsolutePath file))
        (println "")
        (-> (http-response/response file)
            (http-response/content-type content-type))))

(defn handler
    [request]
    (println "request URI: " (request :uri))
    (let [uri (request :uri)]
        (condp = uri
            "/favicon.ico"       (return-file "favicon.ico" "image/x-icon")
            "/bootstrap.min.css" (return-file "bootstrap.min.css" "text/css")
            "/smearch.css"       (return-file "smearch.css" "text/css")
            "/bootstrap.min.js"  (return-file "bootstrap.min.js" "application/javascript")
            "/"                  (perform-normal-processing request)
            "/descriptions"      (render-all-descriptions request))))

