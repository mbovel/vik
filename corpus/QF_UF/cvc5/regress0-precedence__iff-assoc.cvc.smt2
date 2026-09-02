; From cvc5: regress0/precedence/iff-assoc.cvc.smt2 (modified BSD, LICENSES/cvc5.txt)
; EXPECT: unsat
(set-logic ALL)
(set-option :incremental false)
(declare-fun A () Bool)
(declare-fun B () Bool)
(declare-fun C () Bool)
(set-info :status unsat)
(check-sat-assuming ( (not (= (= (= A B) C) (= A (= B C)))) ))
