; Hand-written for VIK (this repository's license): `define-sort` abbreviates
; a sort, with parameters; `declare-sort` with an arity declares a sort
; constructor whose instances are uninterpreted sorts, one per argument
; sort; and a polymorphic `define-fun` is read again per instance.
(set-logic QF_UF)
(declare-sort-parameter X)
(declare-sort Box 1)
(define-sort IntBox () (Box Int))
(define-sort Two (A) (Box (Box A)))
(declare-fun unbox ((Box X)) X)
(define-fun same ((a X) (b X)) Bool (= a b))
(declare-const s IntBox)
(declare-const t (Box Int))
(declare-const w (Two Bool))
(assert (same s t))
(assert (= (unbox s) 1))
(assert (= (unbox (unbox w)) true))
(set-info :status sat)
(check-sat)
(assert (not (same (unbox t) 1)))
(set-info :status unsat)
(check-sat)
