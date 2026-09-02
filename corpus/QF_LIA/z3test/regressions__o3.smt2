; From z3test: o3.smt2 (MIT, LICENSES/z3test.txt)
; Copyright (c) 2015 Microsoft Corporation
(declare-const x Int)
(assert (<= x 2))
(maximize x)
(set-info :status sat)
(check-sat)
(get-objectives)