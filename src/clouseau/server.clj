(ns clouseau.server)

(require '[ring.util.response     :as http-response])
(require '[hiccup.core :as hiccup])
(require '[hiccup.page :as page])
(require '[hiccup.form :as form])

(require '[clojure.java.jdbc :as jdbc])

(require '[clouseau.products :as products])

(defn read-description
    [product package]
    (let [result (jdbc/query (second product) (str "select description from packages where name='" package "';"))
          desc   (:description (first result))]
        (if (not desc)
            [:div {:class "alert alert-danger"} "Not found"]
            [:div {:class "alert alert-success"} (.replaceAll desc "\n" "<br />")])))

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
            ] ; row ends
        ] ; /.container-fluid
]); </nav>

(defn html-renderer
    [products package]
    (page/xhtml
        (render-html-header package)
        [:body
            [:div {:class "container"}
                (render-navigation-bar-section package)

                (if (and package (not (empty? package)))
                    [:table {:class "table table-stripped"}
                        (for [product products]
                            [:div [:div {:class "label label-primary"} (first product)] 
                                  (read-description product package)]
                        )
                    ])
                (render-footer)
            ] ; </div class="container">
        ] ; </body>
    ))

(defn not-empty-parameter?
    [parameter]
    (and parameter (not (empty? parameter))))

(defn perform-normal-processing
    [request]
    (println "time:        " (.toString (new java.util.Date)))
    (println "addr:        " (request :remote-addr))
    (println "params:      " (request :params))
    (println "user-agent:  " ((request :headers) "user-agent"))
    (println "")
    (let [params              (request :params)
          uri                 (request :uri)
          package             (get params "package")]
          (-> (http-response/response (html-renderer products/products package))
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
            (perform-normal-processing request))))

