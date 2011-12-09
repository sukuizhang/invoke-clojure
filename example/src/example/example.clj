(ns example.example
  (:require [invoke-clojure.core :as invoke]
            [example.example1 :as example1]))

(defn f1 [a b] (+ a b))

(defn f2 [a b c] (* a b c))

(defn f3 [u v w] [(f1 u v) (f2 u v w)])

(defn f4 ([u v w] [u v w]) ([a b] [a b]))

(defn person [name sex age] {:name name :sex sex :age age})

(comment
  test urls for example.example
  http://localhost:8080/f1?a=3&b=4
  http://localhost:8080/f2?a=3&b=4&c=7
  http://localhost:8080/f3?&u=3&v=4&w=13
  http://localhost:8080/f4?u=3&v=4&w=13&arg-index=0
  http://localhost:8080/f4_1?a=7&b=8
  http://localhost:8080/person?name="zrr"&sex=:female&age=29

  test urls for example.example1, we can invoke them because it has require by example.example
  http://localhost:8080/example1/f1?a=3&b=4
  http://localhost:8080/example1/f1?a/f2?a=3&b=4&c=7
  http://localhost:8080/example1/f1?a/f3?&u=3&v=4&w=13
  http://localhost:8080/example1/f1?af4?u=3&v=4&w=13&arg-index=0
  http://localhost:8080/example1/f1?af4_1?a=7&b=8
  http://localhost:8080/example1/f1?aperson?name="zrr"&sex=:female&age=29
)
