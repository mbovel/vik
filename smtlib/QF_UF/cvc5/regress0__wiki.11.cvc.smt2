; From cvc5: regress0/wiki.11.cvc.smt2 (modified BSD, LICENSES/cvc5.txt)
; EXPECT: unsat
(set-logic ALL)
(set-option :incremental false)
(declare-fun a () Bool)
(declare-fun b () Bool)
(declare-fun c () Bool)
(set-info :status unsat)
(check-sat-assuming ( (not (= (or a a) a)) ))
