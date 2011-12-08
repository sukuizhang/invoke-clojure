(ns example.example
  (:require [invoke-clojure.core :as invoke]))

(defn f1 [a b] (+ a b))

(defn f2 [a b c] (* a b c))

(defn f3 [u v w] [(f1 u v) (f2 u v w)])

(defn f4 ([u v w] [u v w]) ([a b] [a b]))

(defn person [name sex age] {:name name :sex sex :age age})


;test urls
;http://localhost:8080/invoke?function=f1&a=3&b=4
;http://localhost:8080/invoke?function=f2&a=3&b=4&c=7
;http://localhost:8080/invoke?function=f3&u=3&v=4&w=13
;http://localhost:8080/invoke?function=f4&u=3&v=4&w=13&arg-index=0
;http://localhost:8080/invoke?function=f4&a=7&b=8&arg-index=1
;http://localhost:8080/invoke?function=person&name="zrr"&sex=:female&age=29
