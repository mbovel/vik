; Hand-written for VIK (this repository's license): a constant named `ext!0`, the name the encoder gave
; the first witness index of two arrays asserted different. The witness was the constant, and its value
; the constant's, so arrays agreeing there were equal: a satisfiable script answered unsat (found by the
; adversarial re-check of issue 21, 2026-09-05). The witness passes over the script's names now.
(set-logic QF_ALIA)
(declare-const ext!0 Int) (declare-const ext!1 Int)
(declare-const a (Array Int Int)) (declare-const b (Array Int Int))
(assert (= ext!0 5)) (assert (= ext!1 6))
(assert (= (select a 5) (select b 5))) (assert (= (select a 6) (select b 6)))
(assert (not (= a b)))
(set-info :status sat)
(check-sat)
(assert (= a (store b 7 (select b 7))))
(set-info :status unsat)
(check-sat)
