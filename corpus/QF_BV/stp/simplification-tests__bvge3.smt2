; From stp: simplification-tests/bvge3.smt2 (MIT, LICENSES/stp.txt)
; RUN: %solver --exit-after-CNF %s | %OutputCheck %s
(set-logic QF_BV)
(set-info :smt-lib-version 2.0)
(set-info :category "check")
(set-info :status sat)
(declare-fun x () (_ BitVec 10))
(declare-fun y () (_ BitVec 10))


; 
(assert 
	 (bvslt
		(bvlshr (concat (_ bv0 10)  y )  (_ bv2 20) )
		(concat (_ bv64 10)  x )
	)
)

(assert (not (= x y)))


; CHECK-NEXT: ^sat
(set-info :status sat)
(check-sat)
(exit)

