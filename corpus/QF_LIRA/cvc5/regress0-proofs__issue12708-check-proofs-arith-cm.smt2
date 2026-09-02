; From cvc5: regress0/proofs/issue12708-check-proofs-arith-cm.smt2 (modified BSD, LICENSES/cvc5.txt)
; COMMAND-LINE: --check-proofs
; EXPECT: unsat
(set-logic QF_UFLIRA)
(declare-fun f (Real) Bool)
(declare-const p Int)
(assert (>= p 0))
(assert (not (>= p 1)))
(assert (f 0.0))
(assert (not (f (to_real p))))
(set-info :status unsat)
(check-sat)
