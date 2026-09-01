; Hand-written for VIK (this repository's license): the bit-vector conversions of SMT-LIB 2.7.
(set-logic ALL)
(declare-const x (_ BitVec 8))
(assert (not (= ((_ int_to_bv 8) (ubv_to_int x)) x)))
(set-info :status unsat)
(check-sat)
