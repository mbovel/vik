; From stp: let-tests/let_008.smt2 (CC BY 4.0, as the file states; the suite is MIT, LICENSES/stp.txt)
; RUN: %solver %s | %OutputCheck %s
(set-info :smt-lib-version 2.6)
(set-logic QF_BV)
(set-info :license "https://creativecommons.org/licenses/by/4.0/")
(set-info :category "crafted")
(set-info :status sat)

; Try with distinct.
(assert
	(distinct
		(distinct
			(let
				((x true))
				x
			)

			(let
				((x false))
				x
			)
		)

		(let
			((x (_ bv7 5)))
			(let
				(
					(y (bvnot x))
					(x (= x (_ bv4 5)))
				)
				x
			)
		)

	)
)
; CHECK-NEXT: ^sat
(set-info :status sat)
(check-sat)