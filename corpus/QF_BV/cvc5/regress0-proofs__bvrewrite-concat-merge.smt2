; From cvc5: regress0/proofs/bvrewrite-concat-merge.smt2 (modified BSD, LICENSES/cvc5.txt)
; EXPECT: unsat
(set-logic QF_BV)
(set-info :status unsat)

(declare-fun x1 () (_ BitVec 1))
(declare-fun x2 () (_ BitVec 1))
(declare-fun x3 () (_ BitVec 1))
(declare-fun x4 () (_ BitVec 1))
(assert (not (= (concat x1 x2 x3 x4) (concat (concat x1 x2) (concat x3 x4)))))
(set-info :status unsat)
(check-sat)
(exit)
