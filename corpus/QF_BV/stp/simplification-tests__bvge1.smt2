; From stp: simplification-tests/bvge1.smt2 (MIT, LICENSES/stp.txt)
; RUN: %solver --exit-after-CNF %s | %OutputCheck %s
(set-logic QF_BV)
(set-info :smt-lib-version 2.0)
(set-info :category "check")
(set-info :status unsat)
(declare-fun x () (_ BitVec 10))
(declare-fun y () (_ BitVec 10))


; 
(assert 
	 (bvult
		(bvudiv x  y )
		(_ bv1 10)
	)
)

(assert (not (bvult x y)))


; CHECK-NEXT: ^unsat
(set-info :status unsat)
(check-sat)
(exit)

