; From cvc5: regress0/proofs/qgu-fuzz-4-bool-chainres-postprocessing-singleton.smt2 (modified BSD, LICENSES/cvc5.txt)
; EXPECT: unsat
(set-logic ALL)
(declare-fun b () Bool)
(declare-fun c () Bool)
(declare-fun d () Bool)
(assert (and (or d (ite c false true)) (not (= d (or b c))) (= d (ite c d (not d)))))
(set-info :status unsat)
(check-sat)
