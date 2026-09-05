; Hand-written for VIK (this repository's license): a `store` that occurs only inside the Boolean argument
; of a function. The terms of a Boolean argument reached the encoder when the Booleans were decided, after
; the lemma loop, so the store got no read-over-write axiom and `(g true)` was allowed two values
; (issue 17, 2026-09-05).
(set-logic QF_AUFLIA)
(declare-fun g (Bool) Int)
(declare-const a (Array Int Int))
(declare-const i Int)
(declare-const v Int)
(assert (= (g (= (select (store a i v) i) v)) 0))
(assert (= (g true) 1))
(set-info :status unsat)
(check-sat)
