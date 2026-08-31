; From z3test: core.smt2 (MIT, LICENSES/z3test.txt)
; EXPECT: unsat
; EXPECT: (a)

; Copyright (c) 2015 Microsoft Corporation

(set-logic QF_LRA)
(set-option :auto-config true)
(set-option :produce-unsat-cores true)

(assert (! (= 1 0) :named a))

(set-info :status unsat)
(check-sat)
(get-unsat-core)
