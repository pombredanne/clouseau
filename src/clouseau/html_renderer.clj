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

(ns clouseau.html-renderer)

(require '[hiccup.core            :as hiccup])
(require '[hiccup.page            :as page])
(require '[hiccup.form            :as form])

(defn render-html-header
    "Renders part of HTML page - the header."
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

(defn render-html-footer
    "Renders part of HTML page - the footer."
    []
    [:div "<br /><br /><br /><br />Author: Pavel Tisnovsky &lt;<a href='mailto:ptisnovs@redhat.com'>ptisnovs@redhat.com</a>&gt;&nbsp;&nbsp;&nbsp;"
          "<a href='https://mojo.redhat.com/message/955597'>RFE and general discussion about Clouseau in Mojo</a><br />"])

(defn render-search-field
    "Renders search box on the top side of HTML page."
    [package]
    (form/form-to {:class "navbar-form navbar-left" :role "search"} [:get "/" ]
        [:div {:class "input-group"}
            [:span {:class "input-group-addon"} "Package"]
            (form/text-field {:size "40" :class "form-control" :placeholder "Examples: 'bash', 'vim-enhanced', 'kernel', 'gnome-desktop'"} "package" (str package))
            [:div {:class "input-group-btn"}
                (form/submit-button {:class "btn btn-default"} "Search")]]))

(defn render-name-field
    "Renders box for typing user name on the top side of HTML page."
    [user-name]
    (form/form-to {:class "navbar-form navbar-left" :role "search"} [:get "/" ]
        [:div {:class "input-group"}
            ;[:span {:class "input-group-addon"} "Name"]
            (form/text-field {:size "10" :class "form-control" :placeholder "User name"} "user-name" (str user-name))
            [:div {:class "input-group-btn"}
                (form/submit-button {:class "btn btn-default"} "Remember me")]]))

(defn render-navigation-bar-section
    "Renders whole navigation bar."
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
                (render-html-footer)
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

(defn render-front-page
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

                (render-html-footer)
            ] ; </div class="container">
        ] ; </body>
    ))

(defn render-descriptions
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
                (render-html-footer)
            ] ; </div class="container">
        ] ; </body>
    ))

(defn render-user
    [changes selected-user user-name]
    (page/xhtml
        (render-html-header nil)
        [:body
            [:div {:class "container"}
                (render-navigation-bar-section nil user-name)
                [:h2 "Changes made by " selected-user]
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
                (render-html-footer)
            ] ; </div class="container">
        ] ; </body>
    ))

(defn render-users
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
                (render-html-footer)
            ] ; </div class="container">
        ] ; </body>
    ))

