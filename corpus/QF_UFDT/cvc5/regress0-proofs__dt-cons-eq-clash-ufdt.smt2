; From cvc5: regress0/proofs/dt-cons-eq-clash-ufdt.smt2 (modified BSD, LICENSES/cvc5.txt)
; EXPECT: unsat
; Minimal UFDT example (in the spirit of SMT-LIB cdt-cade2015) that exercises
; dt-cons-eq-clash where the constructor clash (a vs b in the right child) is
; only reached after recursing past a non-constructor sibling argument (the
; uninterpreted-function application (f x) in the left child). The
; $dt_distinct_terms side condition must return false for the non-constructor
; sibling rather than aborting, while still finding the clash.
; logic: QF_UFDT, the smallest the script fits, for UFDT (prune logic)
(set-logic QF_UFDT)
(declare-datatype D ((c (left D) (right D)) (a) (b)))
(declare-fun f (D) D)
(declare-const x D)
(assert (= (c (f x) a) (c a b)))
(set-info :status unsat)
(check-sat)
