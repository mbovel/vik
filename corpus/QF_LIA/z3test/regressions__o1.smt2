; From z3test: o1.smt2 (MIT, LICENSES/z3test.txt)
; Copyright (c) 2015 Microsoft Corporation
; logic: QF_LIA, the smallest the script fits, which set none (prune logic)
(set-logic QF_LIA)
(declare-const x Int)
(maximize x)
(set-info :status sat)
(check-sat)
(get-objectives)
