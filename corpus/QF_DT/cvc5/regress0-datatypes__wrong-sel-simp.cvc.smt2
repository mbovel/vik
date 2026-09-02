; From cvc5: regress0/datatypes/wrong-sel-simp.cvc.smt2 (modified BSD, LICENSES/cvc5.txt)
; EXPECT: sat
(set-logic ALL)
(set-option :incremental false)
(declare-datatypes ((nat 0)) (((succ (pred nat)) (zero))))
(set-info :status sat)
(check-sat-assuming ( (not (= (pred zero) zero)) ))
