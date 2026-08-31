; From z3test: bvcore.smt2 (MIT, LICENSES/z3test.txt)
; EXPECT: unsat
; EXPECT: (a)

; Copyright (c) 2015 Microsoft Corporation

(set-logic QF_BV)
(set-option :auto-config true)
(set-option :produce-unsat-cores true)
(assert (! (= (_ bv0 1) (_ bv1 1)) :named a))

(set-info :status unsat)
(check-sat)
(get-unsat-core)
