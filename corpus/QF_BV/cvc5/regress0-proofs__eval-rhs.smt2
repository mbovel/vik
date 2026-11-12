; From cvc5: regress0/proofs/eval-rhs.smt2 (modified BSD, LICENSES/cvc5.txt)
; EXPECT: unsat
; logic: QF_BV, the smallest the script fits, for ALL (prune logic)
(set-logic QF_BV)
(declare-fun x () (_ BitVec 32))
(assert (not (= ((_ extract 3 1) ((_ extract 7 1) x)) ((_ extract 4 2) x))))
(set-info :status unsat)
(check-sat)
