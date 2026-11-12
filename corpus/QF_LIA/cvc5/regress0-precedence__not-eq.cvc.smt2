; From cvc5: regress0/precedence/not-eq.cvc.smt2 (modified BSD, LICENSES/cvc5.txt)
; EXPECT: unsat
; logic: QF_LIA, the smallest the script fits, for ALL (prune logic)
(set-logic QF_LIA)
(set-option :incremental false)
(declare-fun A () Int)
(declare-fun B () Int)
(set-info :status unsat)
(check-sat-assuming ( (not (let ((_let_1 (not (= A B)))) (= _let_1 _let_1))) ))
