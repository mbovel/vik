; Hand-written for VIK (this repository's license): the bit-vector conversions of SMT-LIB 2.7.
(set-logic ALL)
(declare-const x (_ BitVec 8))
(assert (not (and (<= 0 (ubv_to_int x)) (< (ubv_to_int x) 256))))
(set-info :status unsat)
(check-sat)
