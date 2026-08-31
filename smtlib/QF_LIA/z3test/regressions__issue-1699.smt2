; From z3test: issue-1699.smt2 (MIT, LICENSES/z3test.txt)
; Copyright (c) 2018 Levent Erkok, Microsoft Corporation
; GitHub issue
; EXPECT: sat
; EXPECT: ((define-fun x () Int 0))

(set-option :produce-models true)
(declare-fun x () Int)
(assert (>= 1 x))
(assert (<= 0 x))
(minimize x)
(maximize (+ x x))
(set-option :opt.priority pareto)
(set-info :status sat)
(check-sat)
(get-model)
