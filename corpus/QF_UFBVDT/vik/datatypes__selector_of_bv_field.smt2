; Hand-written for VIK (this repository's license): the selector of a constructor application gives the
; argument when the field is a bit-vector — the lowering declares the field as an integer, or the solver
; never ties the two (wrong `sat` until 2026-09-03, found by the Scala 3 qualified-types checker).
(set-logic QF_UFBVDT)
(declare-datatypes ((Box 0)) (((new_Box (Box.value@Box (_ BitVec 32))))))
(declare-fun x0 () Box)
(assert (= (new_Box (_ bv1 32)) x0))
(assert (not (= (_ bv1 32) (Box.value@Box x0))))
(set-info :status unsat)
(check-sat)
