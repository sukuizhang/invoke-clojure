(ns invoke-clojure.core
  (:use compojure.core, ring.adapter.jetty, compojure.response
        [ring.util.response :only (response content-type)])
  (:require [invoke-clojure.function :as function]
            [compojure.route :as route]            
            [compojure.handler :as handler]))

(defn handle-request [request & [ops]]
  (println request)
  (render
   (let [uri (request :uri)
         f-name (.substring uri 1)]
     (if (= "help" f-name)
       (function/function-args ops)
       (function/invoke f-name (request :params) ops))
     ) request))

(defn mk-handler [& [ops]]
  (-> (fn [request]
        (handle-request request ops))
      (handler/api)))

(def jetty-instance (atom nil))
(defn stop []
  (when @jetty-instance
    (.stop @jetty-instance))
  (reset! jetty-instance nil))

(defn start [& [ops]]
  (let [default-ops {:join? false :port 8080 :host "127.0.0.1"
                     :ns *ns* :remove-ns #{(find-ns 'clojure.core) (find-ns 'invoke.core)}}
        ops (merge default-ops ops)
        handler (mk-handler ops)]
    (if @jetty-instance (stop))
    (reset! jetty-instance
            (run-jetty handler ops))))
