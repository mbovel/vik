; Hand-written for VIK (this repository's license): the bit-vector conversions of SMT-LIB 2.7.
(set-logic ALL)
(declare-const x (_ BitVec 8))
(assert (= (to_real (ubv_to_int x)) 255.0))
(assert (bvult x #xFF))
(set-info :status unsat)
(check-sat)
