; From cvc5: regress0/datatypes/datatype3.cvc.smt2 (modified BSD, LICENSES/cvc5.txt)
; EXPECT: sat
(set-logic ALL)
(set-option :incremental false)
(declare-datatypes ((tree 0)) (((node (left tree) (right tree)) (leaf))))
(declare-fun x () tree)
(declare-fun z () tree)
(set-info :status sat)
(check-sat-assuming ( (not (not (and (and (= (left (left (left (left (left (left (left (left (left (left z)))))))))) x) ((_ is node) z)) (= z x)))) ))
