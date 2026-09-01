; Hand-written for VIK (this repository's license): the bit-vector conversions of SMT-LIB 2.7.
(set-logic ALL)
(assert (= ((_ int_to_bv 8) 256) #x00))
(assert (= ((_ int_to_bv 8) (- 1)) #xFF))
(assert (= ((_ int_to_bv 8) 300) ((_ int_to_bv 8) 44)))
(set-info :status sat)
(check-sat)
