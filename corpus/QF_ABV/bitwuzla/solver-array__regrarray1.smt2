; From bitwuzla: solver/array/regrarray1.smt2 (MIT, LICENSES/bitwuzla.txt)
; logic: QF_ABV, the smallest the script fits, which set none (prune logic)
; statuses: the `unknown` ones answered by Z3 version 4.14.0 (statusstamp, 2026-11-24)
(set-logic QF_ABV)
(set-info :status unsat)
(declare-fun m () (Array (_ BitVec 8) (_ BitVec 8)))
(assert
 (and
  (= (_ bv0 8) (bvand (_ bv1 8) (select m (_ bv0 8))))
  (= (_ bv1 8) (bvand (_ bv1 8) (select m (bvand (_ bv1 8) (select m (bvand (_ bv1 8) (select m (_ bv0 8))))))))))
(check-sat)
