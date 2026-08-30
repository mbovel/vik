; From cvc5: regress0/bv/bvcomp.cvc.smt2 (modified BSD, LICENSES/cvc5.txt)
; EXPECT: unsat
(set-logic ALL)
(set-option :incremental false)
(declare-fun x () (_ BitVec 10))
(set-info :status unsat)
(check-sat-assuming ( (= x (bvnot x)) ))
