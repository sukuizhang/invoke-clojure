(ns invoke-clojure.function
  (:import [java.io ByteArrayOutputStream PrintStream]))

(defn resolve-arglist [arglist]
  (let [resolve-multi-args (fn [args]
                             (or (and (>= (count args) 2)
                                      (= '& (nth args (- (count args) 2)))
                                      (conj (subvec args 0 (- (count args) 2))
                                            'other-args))
                                 args))]
    (->> arglist
         resolve-multi-args
         (map (fn [a b] [(str "arg" a) b]) (iterate inc 1))
         (map (fn [[default arg]] (or (keyword arg) (keyword default)))))))

(defn functions [& [ops]]
  (let [ns (or (:ns ops) *ns*)
        _ (println ns)
        r-ns (or (:remove-ns ops) #{(find-ns 'clojure.core)})
        f-mappings (doto (.getDeclaredField (type *ns*) "mappings")
                     (.setAccessible true))
        mappings (->> (.get (.get f-mappings ns))
                  (filter (fn [[_ var]] (and (var? var) (fn? (var-get var)))))
                  (filter (fn [[_ var]] (not (r-ns (.ns var))))))
        f-aliases (doto (.getDeclaredField (type *ns*) "aliases")
                    (.setAccessible true))
        aliases (->> (.get (.get f-aliases ns))
                     (mapcat (fn [[alias ns]]
                               (map (fn [[sym var]]
                                      [(symbol (str alias "/" sym)) var])
                                    (ns-publics ns))))
                     (filter (fn [[_ var]] (and (var? var) (fn? (var-get var)))))
                     (filter (fn [[_ var]] (not (r-ns (.ns var))))))]
  (-> []
      (into mappings)
      (into aliases))))

(defn function-args
  [& [ops]]
  (->> (functions ops)
       (map (fn [[sym var]]
              (let [arglists (map resolve-arglist
                                  (:arglists (meta var)))]
                (str sym ":" (seq arglists)))))
       (interpose "\n")
       (apply str)))

(defn invoke
  [request & [ops]]
  (println (str "request:" request))
  (let [var-f ((into {} (functions ops)) (symbol (:function request)))
        argindex (if (request :argindex)
                   (Integer/parseInt (request :argindex))
                   0)]
    (cond (not var-f)
          (str "function " (:function request) " not found")
          (not (fn? (var-get var-f)))
          (str (:function request) " not a function ")
          :else
          (let [arg-value (fn [key] (let [t (request key)]
                                     (if t
                                       (load-string t)
                                       nil)))
                args (-> (meta var-f)
                         :arglists
                         (nth argindex)
                         resolve-arglist
                         (#(map arg-value %)))
                f (var-get var-f)]
            (try
              (-> (apply f args) str)
              (catch Exception e
                (let [o (ByteArrayOutputStream.)
                      p (PrintStream. o)]
                  (.printStackTrace e p)
                  (.flush o)
                  (.toString o))))))))
