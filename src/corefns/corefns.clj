(ns corefns.corefns
  (:use [clojure.core.incubator])
  (:refer-clojure :exclude [map nth])
  (:require [corefns.assert_handling :refer :all]
            [corefns.failed_asserts_info :refer :all]))

;; to-do:
;; 1. remove references to contracts, trammel (from project.clj as well) - Done
;; 2. modify our type-checking functions to record the type (or the arg? or the message?) - Done
;; 3. modify error-handling for asserts to check the recorded info - Done
;; 4. rewrite messages similar to the standard ones (perhaps abstract over?) - maybe?
;; 5. don't forget to clear the queue at the end (post-cond? or the end of pre-cond? or finally?) --
;;    not in post-cond since if we got to post-cond, there were no errors. Perhaps after we
;;    process the queue? We aren't going to handle nested errors. finally may be the place - Done
;; 6. Handle multiple-args functions - Done
;; 7. Process function names - Done
;; 8. Change the messsage for only one arg
;; 9. Handle anonymous functions - Done?
;; 10. Add a function name to the error message - Done
;; inf. why didn't I think of this earlier?

;; CONS - look at documentation

;; Including the standard Clojure documentation to make sure that asserts
;; and cases are consistent with the standard Clojure.

;; As of clojure 1.7 allows (map f)
;; (map f coll)
;; (map f c1 c2)
;; (map f c1 c2 c3)
;; (map f c1 c2 c3 & colls)
;; Returns a lazy sequence consisting of the result of applying f to the
;; set of first items of each coll, followed by applying f to the set
;; of second items in each coll, until any one of the colls is
;; exhausted. Any remaining items in other colls are ignored. Function
;; f should accept number-of-colls arguments.
(defn map [argument1 & args]
 {:pre [(check-if-function? "map" argument1)
        (check-if-seqables? "map" args 2)]}
  (apply clojure.core/map argument1 args))

;; count, into, conj, nth, drop, take, concat, filter, reduce
;; Maps and the like: key, val, keys, vals - careful with pre-conds for key!
;; odd?, even?, etc - check for numbers!

;; (count coll)
;; Returns the number of items in the collection. (count nil) returns
;; 0. Also works on strings, arrays, and Java Collections and Maps
(defn count [argument1]
  {:pre [(check-if-seqable? "count" argument1)]}
  (clojure.core/count argument1))

;; (conj coll x)
;; (conj coll x & xs)
;; conj[oin]. Returns a new collection with the xs
;;'added'. (conj nil item) returns (item). The 'addition' may
;; happen at different 'places' depending on the concrete type.
(defn conj [argument1 & args]
  {:pre [(check-if-seqable? "conj" argument1)]}
  (apply clojure.core/conj argument1 args))

;; (into to from)
;; Returns a new coll consisting of to-coll with all of the items of
;; from-coll conjoined.
(defn into [argument1 argument2]
   {:pre [(check-if-seqable? "into" argument1)
          (check-if-seqable? "into" argument2)]}
   (clojure.core/into argument1 argument2))

;; (cons x seq)
;; Returns a new seq where x is the first element and seq is
;;  the rest.
(defn cons [argument1 argument2]
  {:pre [(check-if-seqable? "cons" argument2)]}
  (apply clojure.core/cons argument1 argument2))

;; (reduce f coll)
;; (reduce f val coll)
;; f should be a function of :not-a-function2 arguments. If val is not supplied,
;; returns the result of applying f to the first 2 items in coll, then
;; applying f to that result and the 3rd item, etc. If coll contains no
;; items, f must accept no arguments as well, and reduce returns the
;; result of calling f with no arguments. If coll has only 1 item, it
;; is returned and f is not called. If val is supplied, returns the
;; result of applying f to val and the first item in coll, then
;; applying f to that result and the 2nd item, etc. If coll contains no
;; items, returns val and f is not called.
(defn reduce
  ([argument1 argument2]
   {:pre [(check-if-function? "reduce" argument1)
          (check-if-seqable? "reduce" argument2)]}
   (clojure.core/reduce argument1 argument2))
  ([argument1 argument2 argument3]
   {:pre [(check-if-function? "reduce" argument1)
          (check-if-seqable? "reduce" argument3)]}
   (clojure.core/reduce argument1 argument2 argument3)))

;; (nth coll index)
;; (nth coll index not-found)
;; Returns the value at the index. get returns nil if index out of
;; bounds, nth throws an exception unless not-found is supplied. nth
;; also works for strings, Java arrays, regex Matchers and Lists, and,
;; in O(n) time, for sequences.
(defn nth ;; there may be an optional 3rd arg
  ([argument1 argument2]
   {:pre [(check-if-seqable? "nth" argument1)
          (check-if-number? "nth" argument2)]}
   (clojure.core/nth argument1 argument2))
  ([argument1 argument2 argument3]
   {:pre [(check-if-seqable? "nth" argument1)
          (check-if-number? "nth" argument2)]}
   (clojure.core/nth argument1 argument2 argument3)))

;; As of clojure 1.7 allows (filter f)
;; (filter pred coll)
;; Returns a lazy sequence of the items in coll for which
;; (pred item) returns true. pred must be free of side-effects.
(defn filter
  ([argument1]
   {:pre [(check-if-function? "filter" argument1)]}
   (clojure.core/filter argument1))
  ([argument1 argument2]
   {:pre [(check-if-function? "filter" argument1)
         (check-if-seqable? "filter" argument2)]}
  (clojure.core/filter argument1 argument2)))

;; (mapcat f & colls)
;; Returns the result of applying concat to the result of applying map
;; to f and colls. Thus function f should return a collection.
(defn mapcat [argument1 & args]
  {:pre [(check-if-function? "mapcat" argument1)
         (check-if-seqables? "mapcat" args 2)]}
  (apply clojure.core/mapcat argument1 args))

;; (concat)
;; (concat x)
;; (concat x y)
;; (concat x y & zs)
;; Returns a lazy seq representing the concatenation of the elements in the supplied colls.
(defn concat [& args]
  {:pre [(check-if-seqables? "concat" args 1)]}
  (apply clojure.core/concat args))

;; (drop n coll)
;; Returns a lazy sequence of all but the first n items in coll.
(defn drop [argument1 argument2]
   {:pre [(check-if-number? "drop" argument1)
          (check-if-seqable? "drop" argument2)]}
   (clojure.core/drop argument1 argument2))

;; (take n coll)
;; Returns a lazy sequence of the first n items in coll, or all items if there are fewer than n.
(defn take [argument1 argument2]
   {:pre [(check-if-number? "take" argument1)
          (check-if-seqable? "take" argument2)]}
   (clojure.core/take argument1 argument2))

;;(odd? n)
;;Returns true if n is odd, throws an exception if n is not an integer
(defn odd? [n]
  {:pre [(check-if-integer? "odd?" n 1)]}
  (clojure.core/odd? n))

;;(even? n)
;;Returns true if n is even, throws an exception if n is not an integer
(defn even? [n]
  {:pre [(check-if-integer? "even?" n 1)]}
  (clojure.core/even? n))

;;    (< x)
;;    (< x y)
;;    (< x y & more)
;; Returns non-nil if nums are in monotonically increasing order,
;; otherwise false.
(defn < [argument1 & args]
   {:pre [(check-if-number? "<" argument1)
          (check-if-numbers? "<" args 2)]}
   (apply clojure.core/< argument1 args))

;;    (<= x)
;;    (<= x y)
;;    (<= x y & more)
;; Returns non-nil if nums are in monotonically non-decreasing order,
;; otherwise false.
(defn <= [argument1 & args]
   {:pre [(check-if-number? "<=" argument1)
          (check-if-numbers? "<=" args 2)]}
  (apply clojure.core/<= argument1 args))

;;    (> x)
;;    (> x y)
;;    (> x y & more)
;; Returns non-nil if nums are in monotonically decreasing order,
;; otherwise false.
(defn > [argument1 & args]
   {:pre [(check-if-number? ">" argument1)
          (check-if-numbers? ">" args 2)]}
   (apply clojure.core/> argument1 args))

;;    (>= x)
;;    (>= x y)
;;    (>= x y & more)
;; Returns non-nil if nums are in monotonically non-increasing order,
;; otherwise false.
(defn >= [argument1 & args]
   {:pre [(check-if-number? ">=" argument1)
          (check-if-numbers? ">=" args 2)]}
   (apply clojure.core/>= argument1 args))

;;    (+)
;;    (+ x)
;;    (+ x y)
;;    (+ x y & more)
;; Returns the sum of nums. (+) returns 0. Does not auto-promote longs, will
;; throw on overflow.
(defn + [& args]
  {:pre [(check-if-numbers? "+" args 1)]}
  (apply clojure.core/+ args))

;;    (- x)
;;    (- x y)
;;    (- x y & more)
;; If no ys are supplied, returns the negation of x, else subtracts
;; the ys from x and returns the result.
(defn - [argument1 & args]
  {:pre [(check-if-number? "-" argument1)
         (check-if-numbers? "-" args 2)]}
  (apply clojure.core/- argument1 args))

;;    (*)
;;    (* x)
;;    (* x y)
;;    (* x y & more)
;; Returns the product of nums. (*) returns 1.
(defn * [& args]
  {:pre [(check-if-numbers? "*" args 1)]}
  (apply clojure.core/* args))

;;    (/ x)
;;    (/ x y)
;;    (/ x y & more)
;; If no denominators are supplied, returns 1/numerator,
;; else returns numerator divided by all of the denominators.
(defn / [argument1 & args]
  {:pre [(check-if-number? "/" argument1)
         (check-if-numbers? "/" args 2)]}
  (apply clojure.core// argument1 args))

;;    (quot num div)
;; quot[ient] of dividing numerator by denominator.
(defn quot [argument1 argument2]
  {:pre [(check-if-number? "quot" argument1)
         (check-if-number? "quot" argument2)]}
  (clojure.core/quot argument1 argument2))

;;    (rem num div)
;; remainder of dividing numerator by denominator.
(defn rem [argument1 argument2]
  {:pre [(check-if-number? "rem" argument1)
         (check-if-number? "rem" argument2)]}
  (clojure.core/rem argument1 argument2))

;;    (mod num div)
;; Modulus of num and div. Truncates toward negative infinity.
(defn mod [argument1 argument2]
  {:pre [(check-if-number? "mod" argument1)
         (check-if-number? "mod" argument2)]}
  (clojure.core/mod argument1 argument2))

;;    (inc x)
;; Returns a number one greater than num.
(defn inc [x]
  {:pre [(check-if-number? "inc" x 1)]}
  (clojure.core/inc x))

;;    (dec x)
;; Returns a number one less than num.
(defn dec [x]
  {:pre [(check-if-number? "dec" x 1)]}
  (clojure.core/dec x))

;;    (max x)
;;    (max x y)
;;    (max x y & more)
;; Returns the greatest of the nums.
(defn max [argument1 & args]
  {:pre [(check-if-number? "max" argument1)
         (check-if-numbers? "max" args 2)]}
  (apply clojure.core/max argument1 args))

;;    (min x)
;;    (min x y)
;;    (min x y & more)
;; Returns the least of the nums.
(defn min [argument1 & args]
  {:pre [(check-if-number? "min" argument1)
         (check-if-numbers? "min" args 2)]}
  (apply clojure.core/min argument1 args))
