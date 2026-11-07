; From cvc5: regress0/datatypes/wrong-sel-simp.cvc.smt2 (modified BSD, LICENSES/cvc5.txt)
; EXPECT: sat
; logic: QF_DT, the smallest the script fits, for ALL (prune logic)
(set-logic QF_DT)
(set-option :incremental false)
(declare-datatypes ((nat 0)) (((succ (pred nat)) (zero))))
(set-info :status sat)
(check-sat-assuming ( (not (= (pred zero) zero)) ))
