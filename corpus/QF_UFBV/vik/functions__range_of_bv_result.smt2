; Hand-written for VIK (this repository's license): a bit-vector that comes out of a function application
; is in `[0, 2^n)` like a declared constant — the lowering asserts its range at its first use (wrong `sat`
; until 2026-09-03: the application was an unbounded integer, and `bvadd` on it overflowed wrongly).
(set-logic QF_UFBV)
(declare-fun f ((_ BitVec 8)) (_ BitVec 8))
(declare-fun x () (_ BitVec 8))
(assert (not (bvule (f x) #xFF)))
(set-info :status unsat)
(check-sat)
(reset-assertions)
(assert (= (bvadd (f x) #x01) #x00))
(assert (not (= (f x) #xFF)))
(set-info :status unsat)
(check-sat)
(reset-assertions)
(assert (= (bvadd (f x) #x01) #x00))
(assert (bvsgt x (f x)))
(set-info :status sat)
(check-sat)
