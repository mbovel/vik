; From cvc5: regress0/test9.cvc.smt2 (modified BSD, LICENSES/cvc5.txt)
; EXPECT: unsat
(set-logic ALL)
(set-option :incremental false)
(declare-fun P () Bool)
(declare-fun Q () Bool)
(assert (or P Q))
(set-info :status unsat)
(check-sat-assuming ( (not (or P Q)) ))
