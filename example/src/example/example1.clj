(ns example.example1
  (:require [invoke-clojure.core :as invoke]))

(defn f1 [a b] (+ a b))

(defn f2 [a b c] (* a b c))

(defn f3 [u v w] [(f1 u v) (f2 u v w)])

(defn f4 ([u v w] [u v w]) ([a b] [a b]))

(defn person [name sex age] {:name name :sex sex :age age})

