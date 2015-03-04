;;;
;;;   Clouseau
;;;
;;;   Pavel Tisnovsky <ptisnovs@redhat.com>
;;;

(defproject clouseau "0.1.0-SNAPSHOT"
  :description "Package finder"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [ring/ring-core "1.2.2"]
                 [ring/ring-jetty-adapter "1.2.2"]
                 [hiccup "1.0.4"]
                 [org.clojure/tools.cli "0.3.1"]
                 [org.clojure/java.jdbc "0.3.5"]
                 [org.xerial/sqlite-jdbc "3.7.2"]]
  :dev-dependencies [[lein-ring "0.8.10"]]
  :plugins [[lein-ring "0.8.10"]]
  :ring {:handler clouseau.core/app}
  :main clouseau.core)

