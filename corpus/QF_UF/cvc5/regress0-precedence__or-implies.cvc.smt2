; From cvc5: regress0/precedence/or-implies.cvc.smt2 (modified BSD, LICENSES/cvc5.txt)
; EXPECT: unsat
(set-logic ALL)
(set-option :incremental false)
(declare-fun A () Bool)
(declare-fun B () Bool)
(declare-fun C () Bool)
(set-info :status unsat)
(check-sat-assuming ( (not (let ((_let_1 (=> (or A B) C))) (= _let_1 _let_1))) ))
