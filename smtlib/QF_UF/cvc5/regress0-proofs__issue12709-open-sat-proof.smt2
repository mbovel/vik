; From cvc5: regress0/proofs/issue12709-open-sat-proof.smt2 (modified BSD, LICENSES/cvc5.txt)
; COMMAND-LINE: --check-proofs
; EXPECT: unsat
(set-logic QF_UF)
(declare-const x Bool)
(declare-fun b () Bool)
(declare-fun c () Bool)
(declare-fun d () Bool)
(assert (and (ite c x false) (ite (= x b) (not b) x) (ite d (= b (ite b true d)) b)))
(set-info :status unsat)
(check-sat)
