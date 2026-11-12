; From cvc5: regress0/proofs/qgu-fuzz-2-bool-chainres-checking.smt2 (modified BSD, LICENSES/cvc5.txt)
; EXPECT: unsat
; logic: QF_UF, the smallest the script fits, for ALL (prune logic)
(set-logic QF_UF)
(declare-fun a () Bool)
(declare-fun b () Bool)
(declare-fun c () Bool)
(declare-fun d () Bool)
(assert
(and (ite a c (ite b d c)) (= d (ite a b c)) (not (= a (ite b c d))) (ite a c (ite b d a)) (ite c (ite b a d) c) (ite a c (ite c d a)))
)
(set-info :status unsat)
(check-sat)
