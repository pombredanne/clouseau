(ns clouseau.server)

(require '[ring.util.response     :as http-response])
(require '[hiccup.core :as hiccup])
(require '[hiccup.page :as page])
(require '[hiccup.form :as form])

(require '[clojure.java.jdbc :as jdbc])

(defn handler
    [request]
    (println "request URI: " (request :uri))
    )
