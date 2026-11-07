; From z3test: o7.smt2 (MIT, LICENSES/z3test.txt)
; Copyright (c) 2016 Microsoft Corporation
; logic: QF_LIA, the smallest the script fits, which set none (prune logic)
(set-logic QF_LIA)
(declare-const i Int)
(declare-const j Int)

(assert-soft (> i j) :weight 1 )
(assert-soft (< i j) :weight 40.1 )
(set-info :status sat)
(check-sat)
(get-objectives)
