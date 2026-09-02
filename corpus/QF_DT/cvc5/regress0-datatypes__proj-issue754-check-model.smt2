; From cvc5: regress0/datatypes/proj-issue754-check-model.smt2 (modified BSD, LICENSES/cvc5.txt)
; EXPECT: sat
(set-logic QF_BVDT)
(declare-datatypes ((d 0)) (((c) (_c (s Bool)))))
(declare-const x d)
(set-info :status sat)
(check-sat-assuming ((ite (s c) (s x) (s x))))
