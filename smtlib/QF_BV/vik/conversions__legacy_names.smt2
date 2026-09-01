; Hand-written for VIK (this repository's license): the bit-vector conversions of SMT-LIB 2.7.
; The names before the 2.7 renaming parse to the same operations.
(set-logic ALL)
(declare-const x (_ BitVec 8))
(assert (not (= (bv2nat x) (ubv_to_int x))))
(assert (not (= (bv2int x) (sbv_to_int x))))
(set-info :status unsat)
(check-sat)
