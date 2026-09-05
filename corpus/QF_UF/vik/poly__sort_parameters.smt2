; Hand-written for VIK (this repository's license): a sort parameter
; (`declare-sort-parameter`) makes a rank polymorphic, and each use of the
; symbol at a monomorphic instance — the parameter bound by the arguments'
; sorts, or named with `as` — is a use of an instance declared on demand. The
; instances of `f` at Int, Bool and U are unrelated functions.
; EXPECT: sat
; EXPECT: (((f x) 5) ((f 5) 7) ((f b) true) ((f false) false))
; EXPECT: unsat
(set-logic QF_UFLIA)
(set-option :produce-models true)
(declare-sort-parameter X)
(declare-sort U 0)
(declare-fun f (X) X)
(declare-const x Int)
(declare-const b Bool)
(declare-const u U)
(assert (= (f x) 5))
(assert (= (f (f x)) 7))
(assert (= (f b) true))
(assert (not (f false)))
(assert (distinct (f u) u))
(set-info :status sat)
(check-sat)
(get-value ((f x) (f 5) (f b) (f false)))
(push 1)
(assert (= (f 7) x))
(assert (= x 5))
(set-info :status unsat)
(check-sat)
(pop 1)
