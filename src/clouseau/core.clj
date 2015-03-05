(ns clouseau.core)

(require '[ring.adapter.jetty      :as jetty])
(require '[ring.middleware.params  :as http-params])
(require '[ring.middleware.cookies :as cookies])

(require '[clojure.tools.cli       :as cli])

(require '[clouseau.server         :as server])


(def cli-options
    ;; an option with a required argument
  [["-p" "--port   PORT"    "port number"   :id :port]])

(def app
    (-> server/handler cookies/wrap-cookies http-params/wrap-params))

(defn start-server
    [port]
    (println "Starting the server at the port: " port)
    (jetty/run-jetty app {:port (read-string port)}))

(defn -main
    "Entry point to the Clouseau server."
    [& args]
    (let [all-options      (cli/parse-opts args cli-options)
          options          (all-options :options)
          port             (options :port)]
          (start-server port)))

