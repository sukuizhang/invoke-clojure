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

(defn unwrap-argindex
  [f-name]
  (let [f-name (name f-name)
        idx (.indexOf f-name (int \_))]
    (if (= -1 idx)
      [f-name 0]
      (try
        [(.substring f-name 0 idx) (Integer/parseInt (.substring f-name (+ 1 idx)))]
        (catch NumberFormatException e [f-name 0])))))

(defn invoke
  [f-name f-args & [ops]]
  (try
    (let [[f-name argindex] (unwrap-argindex f-name)
          var-f ((into {} (functions ops)) (symbol f-name))]
      (cond (not var-f)
            (str "function " f-name " not found")
            (not (fn? (var-get var-f)))
            (str f-name " not a function ")
            :else
            (let [arg-value (fn [key] (let [t (f-args key)]
                                       (if t
                                         (load-string (str t))
                                         nil)))
                  args (-> (meta var-f)
                           :arglists
                           (nth argindex)
                           resolve-arglist
                           (#(map arg-value %)))
                  f (var-get var-f)]
              (-> (apply f args) str))))
    (catch Exception e
                (let [o (ByteArrayOutputStream.)
                      p (PrintStream. o)]
                  (.printStackTrace e p)
                  (.flush o)
                  (.toString o)))))
