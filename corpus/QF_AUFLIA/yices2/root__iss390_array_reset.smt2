; From yices2: iss390_array_reset.smt2 (BSD-3-Clause, LICENSES/yices2.txt)
; Issue #390: (reset-assertions) without global-decls must clear the
; array-constant marker (yices_reset_tables recycles term ids). After
; the reset, redeclaring the same name as Int must render as a plain
; (define-fun a () Int ...) and not as an array constant.
(set-logic QF_AUFLIA)
(declare-fun a () (Array Int Int))
(assert (= (select a 0) 1))
(set-info :status sat)
(check-sat)
(reset-assertions)
(declare-fun a () Int)
(assert (= a 99))
(set-info :status sat)
(check-sat)
(get-model)
