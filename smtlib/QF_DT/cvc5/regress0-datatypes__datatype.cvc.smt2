; From cvc5: regress0/datatypes/datatype.cvc.smt2 (modified BSD, LICENSES/cvc5.txt)
; EXPECT: sat
(set-logic ALL)
(set-option :incremental false)
(declare-datatypes ((nat 0)) (((succ (pred nat)) (zero))))
(declare-fun x () nat)
(set-info :status sat)
(check-sat-assuming ( (not (and (not ((_ is succ) x)) (not ((_ is zero) x)))) ))
