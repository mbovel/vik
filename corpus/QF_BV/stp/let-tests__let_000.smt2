; From stp: let-tests/let_000.smt2 (MIT, LICENSES/stp.txt)
; RUN: %solver %s | %OutputCheck %s
(set-logic QF_BV)
(set-info :status sat)

; variables get bound twice
(assert 
	(let 
		((x (_ bv1 6) ) (y (_ bv5 6) ))
		(let 
			((x (_ bv0 6) )  (y x ))
			(not (= x y)) 
		)
	)
)
; CHECK-NEXT: ^sat
(set-info :status sat)
(check-sat)