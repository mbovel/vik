; From yices2: wd/example_mdl2.smt2 (BSD-3-Clause, LICENSES/yices2.txt)
;;
;; Tests for model printing in smt2 format
;;
(set-logic QF_BV)
(declare-const a (_ BitVec 4))
(define-const b (_ BitVec 8) ((_ zero_extend 4) a))
(define-const c (_ BitVec 80) ((_ zero_extend 72) b))
(assert (= (bvmul b b) (_ bv25 8)))
(set-info :status sat)
(check-sat)
(get-model)
(get-value (a b c))
