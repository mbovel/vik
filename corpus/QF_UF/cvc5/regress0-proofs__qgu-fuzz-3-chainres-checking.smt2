; From cvc5: regress0/proofs/qgu-fuzz-3-chainres-checking.smt2 (modified BSD, LICENSES/cvc5.txt)
; EXPECT: unsat
(set-logic ALL)
(declare-fun a () Bool)
(declare-fun c () Bool)
(declare-fun d () Bool)
(assert (and (= a (ite (or c d) d a)) (not (ite d a false)) (ite c a d)))
(set-info :status unsat)
(check-sat)