; From z3test: fp-rounding_mode-3.smt2 (MIT, LICENSES/z3test.txt)
; Copyright (c) 2015 Microsoft Corporation

(set-logic QF_FP)
(set-info :status sat)

(declare-fun rm1 () (RoundingMode))
(declare-fun rm2 () (RoundingMode))

(assert (not (= rm1 rm2)))

(set-info :status sat)
(check-sat)
(set-info :status sat)
(check-sat-using smt)

