; From cvc5: regress0/bv/bvcomp.cvc.smt2 (modified BSD, LICENSES/cvc5.txt)
; EXPECT: unsat
; logic: QF_BV, the smallest the script fits, for ALL (prune logic)
(set-logic QF_BV)
(set-option :incremental false)
(declare-fun x () (_ BitVec 10))
(set-info :status unsat)
(check-sat-assuming ( (= x (bvnot x)) ))
