; From cvc5: regress0/bug32.cvc.smt2 (modified BSD, LICENSES/cvc5.txt)
; EXPECT: unsat
(set-logic ALL)
(set-option :incremental false)
(declare-fun a () Bool)
(declare-fun b () Bool)
(assert a)
(set-info :status unsat)
(check-sat-assuming ( (not (or a b)) ))
