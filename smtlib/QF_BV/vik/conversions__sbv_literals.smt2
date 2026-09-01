; Hand-written for VIK (this repository's license): the bit-vector conversions of SMT-LIB 2.7.
(set-logic ALL)
(assert (= (sbv_to_int #xFF) (- 1)))
(assert (= (sbv_to_int #x7F) 127))
(assert (= (ubv_to_int #xFF) 255))
(set-info :status sat)
(check-sat)
