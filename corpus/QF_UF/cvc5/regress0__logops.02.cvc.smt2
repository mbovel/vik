; From cvc5: regress0/logops.02.cvc.smt2 (modified BSD, LICENSES/cvc5.txt)
; EXPECT: sat
(set-logic ALL)
(set-option :incremental false)
(declare-fun a () Bool)
(declare-fun b () Bool)
(declare-fun c () Bool)
(set-info :status sat)
(check-sat-assuming ( (not (and (not c) b)) ))
