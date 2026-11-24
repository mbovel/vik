; From bitwuzla: solver/array/regrarray3.smt2 (MIT, LICENSES/bitwuzla.txt)
; logic: QF_ABV, the smallest the script fits, which set none (prune logic)
(set-logic QF_ABV)
(set-info :status sat)
(declare-const A (_ BitVec 4))
(declare-fun s () (Array (_ BitVec 4) (_ BitVec 4)))
(assert
 (not
  (= (_ bv0 4) (select (store (store (store (store s (_ bv0 4) (_ bv0 4)) (_ bv1 4) (_ bv0 4)) (_ bv1 4) (_ bv0 4)) (_ bv1 4) (_ bv0 4)) A))))
(check-sat)
