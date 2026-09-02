; From cvc5: regress0/datatypes/datatype4.cvc.smt2 (modified BSD, LICENSES/cvc5.txt)
; EXPECT: sat
(set-logic ALL)
(set-option :incremental false)
(declare-datatypes ((TypeGeneric 0)) (((generic))))
(declare-fun f (TypeGeneric) Int)
(declare-fun a () TypeGeneric)
(set-info :status sat)
(check-sat-assuming ( (not (= (f a) 0)) ))
