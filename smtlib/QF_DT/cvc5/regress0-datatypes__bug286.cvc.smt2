; From cvc5: regress0/datatypes/bug286.cvc.smt2 (modified BSD, LICENSES/cvc5.txt)
; EXPECT: sat
(set-logic ALL)
(set-option :incremental false)
(declare-datatypes ((foo 0)) (((f (i Int)))))
(declare-fun x () foo)
(declare-fun y () Int)
(set-info :status sat)
(check-sat-assuming ( (not (= x (f y))) ))
