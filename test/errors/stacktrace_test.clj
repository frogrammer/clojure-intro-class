(ns errors.stacktrace_test
  (:require [errors.dictionaries :refer :all]
            [expectations :refer :all]
            [corefns.corefns :refer :all]
            [errors.prettify_exception :refer :all]
            [errors.messageobj :refer :all]
            [errors.exceptions :refer :all]))

;;***************************************************
;;***** Testing individual stacktrace elements ******
;;****************************************************

;; a helper function to test for either nil or false
(defn- not-true? [x] (not (true? x)))

(expect not-true? (keep-stack-trace-elem {:method "add",
				     :class "clojure.lang.Numbers",
				     :java true, :file "Numbers.java", :line 126}))

(expect not-true? (keep-stack-trace-elem {:method "eval", :class "clojure.lang.Compiler",
				     :java true, :file "Compiler.java", :line 6619}))

(expect true? (keep-stack-trace-elem {:anon-fn true, :fn "load",
				      :ns "clojure.core", :clojure true,
				      :file "core.clj", :line 5530}))

(expect not-true? (keep-stack-trace-elem {:anon-fn false, :fn "track-reload",
				     :ns "clojure.tools.namespace.reload", :clojure true,
				     :file "reload.clj", :line 52}))

(expect not-true? (keep-stack-trace-elem {:method "main", :class "clojure.main",
					  :java true, :file "main.java", :line 37}))

;; Artificial example (in reality :java true) to test filtering
;; Note: this returns true since "clojure.main" is a class, not a namespace
(expect true? (keep-stack-trace-elem {:method "main", :class "clojure.main",
				      :clojure true, :file "main.java", :line 37}))

(expect not-true? (keep-stack-trace-elem {:method "main", :ns "clojure.main",
					  :clojure true, :file "main.java", :line 37}))

(expect not-true?  (keep-stack-trace-elem
                    {:anon-fn false, :fn "main", :ns "clojure.main", :clojure true,
                     :file "main.clj", :line 420}))

(expect not-true?  (keep-stack-trace-elem
                    {:anon-fn false, :fn "main", :ns "clojure.lang", :clojure true,
                     :file "main.clj", :line 420}))



;;****************************************************
;;********** Testing filtering stacktrace ************
;;****************************************************






;###############################
;### Checks a single element ###
;###############################

;### Does have ###

(defn- helper-trace-elem-has-function? [fun trace-elem]
  (= fun (:fn trace-elem)))

(defn- trace-elem-has-function? [fun]
  (partial helper-trace-elem-has-function? fun))

;~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

(defn- helper-trace-elem-has-pair? [k v trace-elem]
  (= v (k trace-elem)))

(defn- trace-elem-has-pair? [k v]
  (partial helper-trace-elem-has-pair? k v))

;~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

(defn- helper-trace-elem-has-all-pairs? [kv-pairs trace-elem]
  "checks that every binding in kv-pairs also appears in trace-elem"
  (every? true? (map #(helper-trace-elem-has-pair? (first %) (second %) trace-elem) kv-pairs)))

(defn- trace-elem-has-all-pairs? [kv-pairs]
  (partial helper-trace-elem-has-all-pairs? kv-pairs))

;### Doesn't have ###

(defn- helper-trace-elem-doesnt-have-function? [fun trace-elem]
  (not (helper-trace-elem-has-function? fun trace-elem)))

(defn- trace-elem-doesnt-have-function? [fun]
  (partial helper-trace-elem-doesnt-have-function? fun))

;~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

(defn- helper-trace-elem-doesnt-have-pair? [k v trace-elem]
  (not (helper-trace-elem-has-pair? k v trace-elem)))

(defn- trace-elem-doesnt-have-pair? [k v]
  (partial helper-trace-elem-doesnt-have-pair? k v))

;~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
(defn- helper-trace-elem-doesnt-have-all-pairs? [kv-pairs trace-elem]
  "checks that every binding in kv-pairs also appears in trace-elem"
  (not (helper-trace-elem-has-all-pairs? kv-pairs trace-elem)))

(defn- trace-elem-doesnt-have-all-pairs? [kv-pairs]
  (partial helper-trace-elem-doesnt-have-all-pairs? kv-pairs))

;#################################
;### Checks a whole stacktrace ###
;#################################

;; a helper function to test the size of a stacktrace
(defn- helper-stack-count? [n stacktrace]
  (= n (count stacktrace)))

(defn- check-stack-count? [n]
  (partial helper-stack-count? n))

;### Does have ###

(defn- helper-trace-has-function? [fun trace]
  (if (empty? trace) false
    (if (helper-trace-elem-has-function? fun (first trace)) true
      (helper-trace-has-function? fun (rest trace)))))
  ;(any? (trace-elem-has-function? fun) trace))

;(defn- trace-has-function? [fun]
;  (partial helper-trace-elem-has-function? fun))

;~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
;(helper-pair)
;(pair)
;~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
;(helper-all-pairs)
;(all-pairs)



;### Doesn't have ###
;(helper-doesnt-fn)
;(doesnt-fn)
;~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
;(helper-doesnt-pair)
;(doesnt-pair)
;~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
;(helper-doesnt-all-pairs)
;(doesnt-all-pairs)


;; testing the helper function:
(expect true (helper-trace-elem-has-all-pairs? {:fn "map" :ns "corefns.corefns"}
					       {:fn "map" :junk :dontcare :ns "corefns.corefns"}))

(expect false (helper-trace-elem-has-all-pairs? {:fn "map" :ns "corefns.corefns"}
					       {:fn :dontcare :junk "map" :ns "corefns.corefns"}))

;~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

;(defn helper-doesnt-have-pair? [k v stacktrace]
;  (every? (not (= v (k trace-elem))) stacktrace))

;(defn- helper-trace-elem-doesnt-have-pair? [k v trace-elem]
;  (not= v (k trace-elem)))

;(defn helper-trace-doesnt-have-pair? [k v stacktrace]
;  (every? #(helper-trace-elem-doesnt-have-pair? k v %) stacktrace))

;(defn- trace-doesnt-have-pair? [k v]
;  (partial helper-trace-doesnt-have-pair? k v))

;(defn- doesnt-have-pair? [k v]
;  (partial helper-doesnt-have-pair? k v))

;; a stacktrace object copied from a run:
(def complete-stack
  [{:anon-fn false, :fn "map", :ns "corefns.corefns", :clojure true, :file "corefns.clj", :line 34}
   {:method "invoke", :class clojure.lang.RestFn, :java true, :file "RestFn.java", :line 423}
   {:anon-fn false, :fn "eval6415", :ns "intro.core", :clojure true, :file "NO_SOURCE_FILE", :line 302}
   {:method "eval", :class clojure.lang.Compiler, :java true, :file "Compiler.java", :line 6703}
   {:method "eval", :class clojure.lang.Compiler, :java true, :file "Compiler.java", :line 6666}
   {:anon-fn false, :fn "eval", :ns "clojure.core", :clojure true, :file "core.clj", :line 2927}
   {:anon-fn false, :fn "test-and-continue", :ns "intro.core", :clojure true, :file "core.clj", :line 22}
   {:anon-fn true, :fn "map", :ns "clojure.core", :clojure true, :file "core.clj", :line 2559}
   {:method "sval", :class clojure.lang.LazySeq, :java true, :file "LazySeq.java", :line 40}
   {:method "seq", :class clojure.lang.LazySeq, :java true, :file "LazySeq.java", :line 49}
   {:method "seq", :class clojure.lang.RT, :java true, :file "RT.java", :line 484}
   {:anon-fn false, :fn "seq", :ns "clojure.core", :clojure true, :file "core.clj", :line 133}
   {:anon-fn false, :fn "dorun", :ns "clojure.core", :clojure true, :file "core.clj", :line 2855}
   {:anon-fn false, :fn "doall", :ns "clojure.core", :clojure true, :file "core.clj", :line 2871}
   {:anon-fn false, :fn "test-all-and-continue", :ns "intro.core", :clojure true, :file "core.clj", :line 28}
   {:anon-fn false, :fn "test-asserts", :ns "intro.core", :clojure true, :file "core.clj", :line 301}
   {:anon-fn false, :fn "-main", :ns "intro.core", :clojure true, :file "core.clj", :line 599}
   {:method "invoke", :class clojure.lang.RestFn, :java true, :file "RestFn.java", :line 397}
   {:method "invoke", :class clojure.lang.Var, :java true, :file "Var.java", :line 375}
   {:anon-fn false, :fn "eval6408", :ns "user", :clojure true, :file "NO_SOURCE_FILE", :line 1}
   {:method "eval", :class clojure.lang.Compiler, :java true, :file "Compiler.java", :line 6703}
   {:method "eval", :class clojure.lang.Compiler, :java true, :file "Compiler.java", :line 6693}
   {:method "eval", :class clojure.lang.Compiler, :java true, :file "Compiler.java", :line 6666}
   {:anon-fn false, :fn "eval", :ns "clojure.core", :clojure true, :file "core.clj", :line 2927}
   {:anon-fn false, :fn "eval-opt", :ns "clojure.main", :clojure true, :file "main.clj", :line 288}
   {:anon-fn false, :fn "initialize", :ns "clojure.main", :clojure true, :file "main.clj", :line 307}
   {:anon-fn false, :fn "null-opt", :ns "clojure.main", :clojure true, :file "main.clj", :line 342}
   {:anon-fn false, :fn "main", :ns "clojure.main", :clojure true, :file "main.clj", :line 420}
   {:method "invoke", :class clojure.lang.RestFn, :java true, :file "RestFn.java", :line 421}
   {:method "invoke", :class clojure.lang.Var, :java true, :file "Var.java", :line 383}
   {:method "applyToHelper", :class clojure.lang.AFn, :java true, :file "AFn.java", :line 156}
   {:method "applyTo", :class clojure.lang.Var, :java true, :file "Var.java", :line 700}
   {:method "main", :class clojure.main, :java true, :file "main.java", :line 37}])

(def filtered-stack
  [{:anon-fn false, :fn "map", :ns "corefns.corefns", :clojure true, :file "corefns.clj", :line 34}
   {:anon-fn false, :fn "eval6415", :ns "intro.core", :clojure true, :file "NO_SOURCE_FILE", :line 302}
   {:anon-fn false, :fn "eval", :ns "clojure.core", :clojure true, :file "core.clj", :line 2927}
   {:anon-fn false, :fn "test-and-continue", :ns "intro.core", :clojure true, :file "core.clj", :line 22}
   {:anon-fn true, :fn "map", :ns "clojure.core", :clojure true, :file "core.clj", :line 2559}
   {:anon-fn false, :fn "seq", :ns "clojure.core", :clojure true, :file "core.clj", :line 133}
   {:anon-fn false, :fn "dorun", :ns "clojure.core", :clojure true, :file "core.clj", :line 2855}
   {:anon-fn false, :fn "doall", :ns "clojure.core", :clojure true, :file "core.clj", :line 2871}
   {:anon-fn false, :fn "test-all-and-continue", :ns "intro.core", :clojure true, :file "core.clj", :line 28}
   {:anon-fn false, :fn "test-asserts", :ns "intro.core", :clojure true, :file "core.clj", :line 301}
   {:anon-fn false, :fn "-main", :ns "intro.core", :clojure true, :file "core.clj", :line 599}
   ;{:anon-fn false, :fn "eval6408", :ns "user", :clojure true, :file "NO_SOURCE_FILE", :line 1}
   {:anon-fn false, :fn "eval", :ns "clojure.core", :clojure true, :file "core.clj", :line 2927}])

(def filtered-stack2
  [{:anon-fn false, :fn "eval9481", :ns "experimental.core-test", :clojure true,
:file "core_test.clj", :line 57}
   {:anon-fn false, :fn "eval", :ns "clojure.core", :clojure true, :file "core.clj", :line 2852}
   {:anon-fn false, :fn "run-and-catch", :ns "experimental.core-test", :clojure true, :file "core_test.clj", :line 38}
   {:anon-fn true, :fn "load", :ns "clojure.core", :clojure true, :file "core.clj", :line 5530}
   {:anon-fn false, :fn "load", :ns "clojure.core", :clojure true, :file "core.clj", :line 5529}
   {:anon-fn false, :fn "load-one", :ns "clojure.core", :clojure true, :file "core.clj", :line 5336}
   {:anon-fn true, :fn "load-lib", :ns "clojure.core", :clojure true, :file "core.clj", :line 5375}
   {:anon-fn false, :fn "load-lib", :ns "clojure.core", :clojure true, :file "core.clj", :line 5374}
   {:anon-fn false, :fn "apply", :ns "clojure.core", :clojure true, :file "core.clj", :line 619}
   {:anon-fn false, :fn  "load-libs", :ns "clojure.core", :clojure true, :file "core.clj", :line 5413}
   {:anon-fn false, :fn "apply", :ns "clojure.core", :clojure true, :file "core.clj", :line 619}
   {:anon-fn false, :fn "require", :ns "clojure.core", :clojure true, :file "core.clj", :line 5496}
   {:anon-fn false, :fn "alter-var-root", :ns "clojure.core", :clojure true, :file "core.clj", :line 4946}
   ])

(def complete-stack2
[{:method "add", :class "clojure.lang.Numbers", :java true, :file "Numbers.java", :line 126}
{:method "add", :class "clojure.lang.Numbers", :java true, :file "Numbers.java", :line 3523}
{:anon-fn false, :fn "eval9481", :ns "experimental.core-test", :clojure true,
:file "core_test.clj", :line 57}
{:method "eval", :class "clojure.lang.Compiler"
, :java true, :file "Compiler.java", :line 6619}
{:method "eval", :class "clojure.lang.Compiler", :java true, :file "Compiler.java", :line 6582}
{:anon-fn false, :fn "eval", :ns "clojure.core", :clojure true, :file "core.clj", :line 2852}
{:anon-fn false, :fn "run-and-catch", :ns "experimental.core-test", :clojure true, :file "core_test.clj", :line 38}
{:method "applyToHelper", :class "clojure.lang.AFn", :java true, :file "AFn.java", :line 161}
{:method "applyTo", :class "clojure.lang.AFn", :java true, :file "AFn.java", :line 151}
{:method "eval", :class
 "clojure.lang.Compiler$InvokeExpr", :java true, :file "Compiler.java", :line 3458}
{:method "eval", :class "clojure.lang.Compiler$DefExpr", :java true, :file "Compiler.java", :line 408}
{:method "eval", :class "clojure.lang.Compiler", :java true, :file "Compiler.java", :line 6624}
{:method "load", :class "clojure.lang.Compiler", :java true, :file "Compiler.java", :line 7064}
{:method "loadResourceScript", :class "clojure.lang.RT", :java true, :file "RT.java", :line 370}
{:method "loadResourceScript", :class "clojure.lang.RT", :java true, :file "RT.java", :line 361}
{:method "load", :class "clojure.lang.RT", :java true, :file "RT.java", :line 440}
{:method "load", :class "clojure.lang.RT", :java true, :file "RT.java", :line 411}
{:anon-fn true, :fn "load", :ns "clojure.core", :clojure true, :file "core.clj", :line 5530}
{:anon-fn false, :fn "load", :ns "clojure.core",
 :clojure true, :file "core.clj", :line 5529}
{:method "invoke", :class "clojure.lang.RestFn", :java true, :file "RestFn.java", :line 408}
{:anon-fn false, :fn "load-one", :ns "clojure.core", :clojure true, :file "core.clj", :line 5336}
{:anon-fn true, :fn "load-lib", :ns "clojure.core", :clojure true, :file "core.clj"
, :line 5375}
{:anon-fn false, :fn "load-lib", :ns "clojure.core", :clojure true, :file "core.clj", :line 5374}
{:method "applyTo", :class "clojure.lang.RestFn"
, :java true, :file "RestFn.java", :line 142}
{:anon-fn false, :fn "apply", :ns "clojure.core", :clojure true, :file "core.clj", :line 619}
{:anon-fn false, :fn  "load-libs", :ns "clojure.core", :clojure true, :file "core.clj", :line 5413}
{:method "applyTo", :class "clojure.lang.RestFn", :java true, :file "RestFn.java"
, :line 137}
{:anon-fn false, :fn "apply", :ns "clojure.core", :clojure true, :file "core.clj", :line 619}
{:anon-fn false, :fn "require", :ns "clojure.core", :clojure true, :file "core.clj", :line 5496}
{:method "invoke", :class "clojure.lang.RestFn", :java true, :file "RestFn.java", :line 421}
{:anon-fn false, :fn "track-reload-one", :ns "clojure.tools.namespace.reload", :clojure true, :file "reload.clj", :line 35}
{:anon-fn false, :fn "track-reload", :ns "clojure.tools.namespace.reload", :clojure true, :file "reload.clj", :line 52}
{:method "applyToHelper", :class "clojure.lang.AFn", :java true, :file "AFn.java", :line 161}
{:method "applyTo", :class "clojure.lang.AFn", :java true, :file "AFn.java", :line 151}
{:method "alterRoot", :class "clojure.lang.Var", :java true, :file "Var.java"
, :line 336}
{:anon-fn false, :fn "alter-var-root", :ns "clojure.core", :clojure
 true, :file "core.clj", :line 4946}
{:method "invoke", :class "clojure.lang.RestFn", :java true, :file "RestFn.java", :line 425}
{:anon-fn false, :fn "do-refresh", :ns "clojure.tools.namespace.repl", :clojure true, :file "repl.clj", :line 94}
{:anon-fn false, :fn "refresh", :ns "clojure.tools.namespace.repl", :clojure
 true, :file "repl.clj", :line 142}
{:method "invoke", :class "clojure.lang.RestFn", :java true, :file "RestFn.java", :line 397}
{:anon-fn false, :fn "refresh-environment", :ns "autoexpect.runner", :clojure true, :file "runner.clj", :line 23}
{:anon-fn true, :fn "run-tests", :ns "autoexpect.runner", :clojure true, :file "runner.clj", :line 50}
{:anon-fn false, :fn "run-tests", :ns "autoexpect.runner", :clojure true, :file "runner.clj", :line 50}
{:anon-fn true, :fn "monitor-project", :ns "autoexpect.runner", :clojure true, :file "runner.clj", :line 69}
{:anon-fn true, :fn "monitor-project", :ns "autoexpect.runner", :clojure true, :file "runner.clj", :line 68}
{:anon-fn false, :fn "monitor-project", :ns "autoexpect.runner", :clojure true, :file "runner.clj", :line 66}
{:anon-fn false, :fn "eval1187", :ns "user", :clojure true, :file "form-init6834699387848419871.clj",
:line 1}
{:method "eval", :class "clojure.lang.Compiler", :java true, :file "Compiler.java", :line 6619}
{:method "eval", :class "clojure.lang.Compiler", :java true, :file "Compiler.java", :line 6609}
{:method "load", :class "clojure.lang.Compiler", :java true, :file "Compiler.java", :line 7064}
{:method "loadFile", :class "clojure.lang.Compiler", :java true, :file "Compiler.java", :line 7020}
{:anon-fn false, :fn "load-script", :ns "clojure.main", :clojure true, :file "main.clj", :line 294}
{:anon-fn false, :fn "init-opt", :ns "clojure.main", :clojure true, :file "main.clj", :line 299}
{:anon-fn false, :fn "initialize", :ns "clojure.main", :clojure true, :file "main.clj", :line 327}
{:anon-fn false, :fn "null-opt", :ns "clojure.main", :clojure true, :file "main.clj", :line 362}
{:anon-fn false, :fn "main", :ns "clojure.main", :clojure true, :file "main.clj", :line 440}
{:method "invoke", :class "clojure.lang.RestFn", :java true, :file "RestFn.java", :line 421}
{:method "invoke", :class "clojure.lang.Var", :java true, :file "Var.java", :line 419}
{:method "applyToHelper", :class "clojure.lang.AFn", :java true, :file "AFn.java", :line 163}
{:method "applyTo", :class "clojure.lang.Var", :java true, :file "Var.java", :line 532}
{:method "main", :class "clojure.main", :java true, :file "main.java", :line 37}])

;#############
;### Tests ###
;#############

;; testing for filter-stacktrace
(expect filtered-stack (filter-stacktrace complete-stack))
(expect filtered-stack2 (filter-stacktrace complete-stack2))
(expect (check-stack-count? 5) (filter-stacktrace complete-stack2))

(expect {:anon-fn true, :fn "map", :ns "clojure.core", :clojure true, :file "core.clj", :line 2559}
	(in (filter-stacktrace complete-stack)))

;; testing for helper-trace-elem-has-function?
(expect true (helper-trace-elem-has-function? "map" {:fn "map" :ns "corefns.corefns"}))
(expect false (helper-trace-elem-has-function? "soup" {:fn "map" :ns "corefns.corefns"}))

;; testing for trace-elem-has-function?
(expect (trace-elem-has-function? "map") (in (filter-stacktrace complete-stack)))
(expect true ((trace-elem-has-function? "map") {:fn "map" :ns "corefns.corefns"}))
(expect false ((trace-elem-has-function? "donkey") {:fn "map" :ns "corefns.corefns"}))

;; testing for helper-trace-elem-has-pair?
(expect true (helper-trace-elem-has-pair? :ns "corefns.corefns" {:fn "map" :ns "corefns.corefns"}))
(expect false (helper-trace-elem-has-pair? :emma "lemmon" {:fn "map" :ns "corefns.corefns"}))

;; testing for trace-elem-has-pair?
(expect (trace-elem-has-pair? :fn "-main") (in (filter-stacktrace complete-stack)))
(expect true ((trace-elem-has-pair? :ns "corefns.corefns") {:fn "map" :ns "corefns.corefns"}))
(expect false ((trace-elem-has-pair? :emma "lemmon") {:fn "map" :ns "corefns.corefns"}))

;; testing for helper-trace-elem-has-all-pairs?
(expect true (helper-trace-elem-has-all-pairs? {:ns "corefns.corefns", :fn "map"} {:fn "map" :ns "corefns.corefns"}))
(expect false (helper-trace-elem-has-all-pairs? {:emma "lemmon", :not "pass"} {:fn "map" :ns "corefns.corefns"}))

;; testing for trace-elem-has-all-pairs?
(expect (trace-elem-has-all-pairs? {:fn "test-and-continue", :ns "intro.core"}) (in (filter-stacktrace complete-stack)))
(expect true ((trace-elem-has-all-pairs? {:ns "corefns.corefns" :fn "map"}) {:fn "map" :ns "corefns.corefns"}))
(expect false ((trace-elem-has-all-pairs? {:emma "lemmon" :not "pass"}) {:fn "map" :ns "corefns.corefns"}))

;; testing for helper-trace-elem-doesnt-have-function?
(expect false (helper-trace-elem-doesnt-have-function? "map" {:fn "map" :ns "corefns.corefns"}))
(expect true (helper-trace-elem-doesnt-have-function? "soup" {:fn "map" :ns "corefns.corefns"}))

;; testing for trace-elem-doesnt-have-function?
(expect (trace-elem-doesnt-have-function? "smooth") (in (filter-stacktrace complete-stack)))
(expect false ((trace-elem-doesnt-have-function? "map") {:fn "map" :ns "corefns.corefns"}))
(expect true ((trace-elem-doesnt-have-function? "donkey") {:fn "map" :ns "corefns.corefns"}))

;; testing for helper-trace-elem-doesnt-have-pair?
(expect false (helper-trace-elem-doesnt-have-pair? :ns "corefns.corefns" {:fn "map" :ns "corefns.corefns"}))
(expect true (helper-trace-elem-doesnt-have-pair? :emma "lemmon" {:fn "map" :ns "corefns.corefns"}))

;; testing for trace-elem-doesnt-have-pair?
(expect (trace-elem-doesnt-have-pair? :fn "hippo") (in (filter-stacktrace complete-stack)))
(expect false ((trace-elem-doesnt-have-pair? :ns "corefns.corefns") {:fn "map" :ns "corefns.corefns"}))
(expect true ((trace-elem-doesnt-have-pair? :emma "lemmon") {:fn "map" :ns "corefns.corefns"}))

;; testing for helper-trace-elem-doesnt-have-all-pairs?
(expect false (helper-trace-elem-doesnt-have-all-pairs? {:ns "corefns.corefns", :fn "map"} {:fn "map" :ns "corefns.corefns"}))
(expect true (helper-trace-elem-doesnt-have-all-pairs? {:emma "lemmon", :not "pass"} {:fn "map" :ns "corefns.corefns"}))

;; testing for trace-elem-doesnt-have-all-pairs?
(expect (trace-elem-doesnt-have-all-pairs? {:fn "test-and-continue", :ns "rhino.squirrel"}) (in (filter-stacktrace complete-stack)))
(expect false ((trace-elem-doesnt-have-all-pairs? {:ns "corefns.corefns" :fn "map"}) {:fn "map" :ns "corefns.corefns"}))
(expect true ((trace-elem-doesnt-have-all-pairs? {:emma "lemmon" :not "pass"}) {:fn "map" :ns "corefns.corefns"}))

;~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

;; testing for helper-stack-count?
(expect (helper-stack-count? 2 [:z :q]))
(expect (helper-stack-count? 0 []))

;; testing for check-stack-count?
(expect (check-stack-count? 3) [3 6 7])
(expect (check-stack-count? 0) [])
(expect (check-stack-count? 12) (filter-stacktrace complete-stack))

;; testing for helper-trace-has-function?
(expect true (helper-trace-has-function? "map" filtered-stack))
(expect false (helper-trace-has-function? "soup" filtered-stack))

;; testing for trace-elem-has-function?
;(expect (trace-elem-has-function? "map") (in (filter-stacktrace complete-stack)))
;(expect true ((trace-elem-has-function? "map") {:fn "map" :ns "corefns.corefns"}))
;(expect false ((trace-elem-has-function? "donkey") {:fn "map" :ns "corefns.corefns"}))



;(expect (doesnt-have-pair? :walrus "monkey") (filter-stacktrace complete-stack))

;; this test below should fail
;(expect false (doesnt-have-pair? :clojure true) (filter-stacktrace complete-stack))

;; we can combine conditions if we want to, do we want to?

;(expect (more filtered-stack (check-stack-count? 13))
;	(filter-stacktrace complete-stack))

