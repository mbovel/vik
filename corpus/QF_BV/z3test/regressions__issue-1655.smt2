; From z3test: issue-1655.smt2 (MIT, LICENSES/z3test.txt)
; Copyright (c) 2018 Microsoft Corporation
; GitHub issue
; EXPECT: sat
; EXPECT: ((m #b00000000))
; EXPECT: ((x #b00000000))

(set-logic QF_BV)
(set-option :produce-models true)
(declare-fun x () (_ BitVec 8))
(declare-fun m () (_ BitVec 8))
(assert (= m x))
(maximize x)
(set-info :status sat)
(check-sat)
(get-objectives)
(get-value (m))
(get-value (x))
