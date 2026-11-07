; From cvc5: regress0/precedence/and-not.cvc.smt2 (modified BSD, LICENSES/cvc5.txt)
; EXPECT: unsat
; logic: QF_UF, the smallest the script fits, for ALL (prune logic)
(set-logic QF_UF)
(set-option :incremental false)
(declare-fun A () Bool)
(declare-fun B () Bool)
(set-info :status unsat)
(check-sat-assuming ( (not (let ((_let_1 (and A (not B)))) (= _let_1 _let_1))) ))
