; From cvc5: regress0/bv/sizecheck.cvc.smt2 (modified BSD, LICENSES/cvc5.txt)
; EXPECT: sat
(set-logic ALL)
(set-option :incremental false)
(declare-fun x () (_ BitVec 10))
(declare-fun y () (_ BitVec 12))
(assert (= #b0001000000000000 (bvadd ((_ zero_extend 6) x) ((_ zero_extend 4) y))))
(set-info :status sat)
(check-sat)
