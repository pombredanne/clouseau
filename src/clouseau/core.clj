;;;
;;;   Clouseau
;;; 
;;;    Copyright (C) 2015 Pavel Tisnovsky <ptisnovs@redhat.com>
;;; 
;;; Bytecode synth is free software; you can redistribute it and/or modify
;;; it under the terms of the GNU General Public License as published by
;;; the Free Software Foundation; either version 2, or (at your option)
;;; any later version.
;;; 
;;; Bytecode synth is distributed in the hope that it will be useful, but
;;; WITHOUT ANY WARRANTY; without even the implied warranty of
;;; MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
;;; General Public License for more details.
;;; 
;;; You should have received a copy of the GNU General Public License
;;; along with Bytecode synth; see the file COPYING.  If not, write to the
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
    "Ring application."
    (-> server/handler
        cookies/wrap-cookies
        http-params/wrap-params))

(defn start-server
    "Start server on specified port."
    [port]
    (println "Starting the server at the port: " port)
    (jetty/run-jetty app {:port (read-string port)}))

(defn get-port
    "Returns specified port or default port if none is specified on the command line."
    [specified-port]
    (or specified-port "3000"))

(defn -main
    "Entry point to the Clouseau server."
    [& args]
    (let [all-options      (cli/parse-opts args cli-options)
          options          (all-options :options)
          port             (options :port)]
          (start-server (get-port port))))

