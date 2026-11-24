; From yices2: iss390_array_define_sort.smt2 (BSD-3-Clause, LICENSES/yices2.txt)
; Issue #390: define-sort macros that expand to an Array sort must
; still trigger the SMT-LIB store/const rendering in models.
(set-logic QF_AUFLIA)
(define-sort A () (Array Int Int))
(declare-fun a () A)
(assert (= (select a 0) 1))
(set-info :status sat)
(check-sat)
(get-model)
