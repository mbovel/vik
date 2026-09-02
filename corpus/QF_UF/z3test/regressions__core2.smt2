; From z3test: core2.smt2 (MIT, LICENSES/z3test.txt)
; EXPECT: unsat
; EXPECT: ()

; Copyright (c) 2015 Microsoft Corporation

(set-option :produce-unsat-cores true)

(declare-const a Bool)
(assert (or (not a) (= 1 0)))
(set-info :status unsat)
(check-sat a)
(get-unsat-core)
