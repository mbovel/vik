; From z3test: issue-1608.smt2 (MIT, LICENSES/z3test.txt)
; Copyright (c) 2018 Microsoft Corporation
; GitHub issue

(declare-const x Real)
(assert (is_int x))
(assert (not (is_int (+ x 1))))
(set-info :status unsat)
(check-sat)