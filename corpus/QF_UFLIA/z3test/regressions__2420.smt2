; From z3test: 2420.smt2 (MIT, LICENSES/z3test.txt)
; EXPECT: sat
; EXPECT: ((define-fun q ((x!0 Int)) Int 0))
; EXPECT: ((q q))
(declare-fun q (Int) Int)
(set-info :status sat)
(check-sat)
(get-model)
(get-value (q))
