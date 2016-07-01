(ns corefns.specs
  (:require [clojure.spec :as s]))

;;                        Spec for clojure.spec  by Tony
;; ==================================================================================|
;; * For every function with inlining, we can't use spec without overwriting it.     |
;; * put spec before a funtion -> specify conditions for the core function           |
;;   vs                                                                              |
;;   put spec after a function -> specify conditions for the overwritten function    |
;; * 'NO' means the function doesn't need to be overwritten                          |
;;   'O' means the function should be overwritten                                    |
;; ==================================================================================|


; NO
(s/fdef empty?
  :args (s/cat :check-seqable seqable?))
(s/instrument #'empty?)

; NO
(s/fdef map
      :args (s/cat :check-function ifn? :check-seqable (s/+ seqable?)))
(s/instrument #'map)

; NO
(s/fdef conj
  :args (s/cat :check-seqable seqable? :dummy (s/+ ::s/any)))
(s/instrument #'conj)

; NO
(s/fdef into
      :args (s/cat :check-seqable seqable? :check-seqable seqable?))
(s/instrument #'into)

; NO
(s/fdef cons
      :args (s/cat :dummy ::s/any :check-seqable seqable?))
(s/instrument #'cons)

; NO
(s/fdef reduce
        :args (s/cat :check-funtion ifn? :dummy (s/? ::s/any) :check-seqable seqable?))
(s/instrument #'reduce)

; O - TODO: doesn't work for unknown reason
;; (s/fdef nth
;;   :args (s/cat :check-seqable seqable? :check-number number? :dummy (s/? ::s/any)))
;; (s/instrument #'nth)

; NO
(s/fdef filter
  :args (s/cat :check-function ifn? :check-seqable seqable?))
(s/instrument #'filter)

; NO
(s/fdef mapcat
  :args (s/cat :check-function ifn? :check-seqable (s/+ seqable?)))
(s/instrument #'mapcat)

; NO -> TODO: FIXME
;; (s/fdef assoc
;;   :args (s/cat :check-map-or-vector (s/or :check-map map? :check-vector vector?)
;;                :dummy ::s/any
;;                :dummies (s/+ ::s/any)))
;; (s/instrument #'assoc)

; NO
(s/fdef dissoc
  :args (s/cat :check-map map? :dummies (s/* ::s/any)))
(s/instrument #'dissoc)

; NO
(s/fdef odd?
  :args (s/cat :check-integer integer?))
(s/instrument #'odd?)
