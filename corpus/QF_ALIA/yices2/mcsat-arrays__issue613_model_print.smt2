; From yices2: mcsat/arrays/issue613_model_print.smt2 (BSD-3-Clause, LICENSES/yices2.txt)
(set-option :produce-models true)
(set-logic QF_ALIA)

(declare-fun a () (Array Int (Array Int Int)))

(assert (= (select (select a 1) 7) 10))

(set-info :status sat)
(check-sat)
(get-model)
(get-value (a))
(get-value ((select a 1)))
(get-value ((select (select a 1) 7)))
