; Hand-written for VIK (this repository's license): the bit-vector conversions of SMT-LIB 2.7.
(set-logic ALL)
(declare-const n Int)
(assert (= ((_ int_to_bv 8) n) #x2A))
(assert (<= 0 n))
(assert (< n 256))
(assert (not (= n 42)))
(set-info :status unsat)
(check-sat)
