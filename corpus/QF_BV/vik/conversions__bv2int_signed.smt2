; Hand-written for VIK (this repository's license): `bv2int` is signed, as the SMT-LIB theory defines it.
; z3 disagrees: its `bv2int` is unsigned, the same as `bv2nat`, so z3 answers sat on the second check.
(set-logic ALL)
(push 1)
(assert (not (= (bv2int #x7F) 127)))
(set-info :status unsat)
(check-sat)
(pop 1)
(assert (not (= (bv2int #xFF) (- 1))))
(set-info :status unsat)
(check-sat)
