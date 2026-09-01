; From z3test: o6.smt2 (MIT, LICENSES/z3test.txt)
; Copyright (c) 2016 Microsoft Corporation
(declare-fun k () Int)
(assert (< (to_real k) 2))
(maximize k)
(set-info :status sat)
(check-sat)
(get-objectives)