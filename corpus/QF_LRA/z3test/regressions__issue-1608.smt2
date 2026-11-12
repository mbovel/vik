; From z3test: issue-1608.smt2 (MIT, LICENSES/z3test.txt)
; Copyright (c) 2018 Microsoft Corporation
; GitHub issue
; logic: QF_LRA, the smallest the script fits, which set none (prune logic)
(set-logic QF_LRA)

(declare-const x Real)
(assert (is_int x))
(assert (not (is_int (+ x 1))))
(set-info :status unsat)
(check-sat)
