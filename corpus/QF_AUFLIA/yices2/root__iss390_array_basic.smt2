; From yices2: iss390_array_basic.smt2 (BSD-3-Clause, LICENSES/yices2.txt)
; Issue #390: SMT-LIB Array-declared constants must be printed in the
; model as 0-arity (define-fun name () (Array K V) (store ... (as const ...) ...))
; rather than as a function (define-fun name ((x!0 K)) V (ite ...)).
(set-logic QF_AUFLIA)
(declare-fun a () (Array Int Int))
(assert (= (select a 0) 1))
(assert (= (select a 1) 3))
(assert (= (select a 2) 5))
(set-info :status sat)
(check-sat)
(get-model)
