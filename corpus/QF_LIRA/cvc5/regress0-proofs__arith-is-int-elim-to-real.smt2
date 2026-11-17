; From cvc5: regress0/proofs/arith-is-int-elim-to-real.smt2 (modified BSD, LICENSES/cvc5.txt)
; DISABLE-TESTER: alethe
; EXPECT: unsat
(set-logic QF_LIRA)
(declare-const x Int)
(assert (not (is_int (to_real x))))
(set-info :status unsat)
(check-sat)
