; From opensmt: base/QF_LRA/bignum_lra2_simplified1.smt2 (MIT, LICENSES/opensmt.txt)
(set-logic QF_LRA)
(declare-fun v () Real)
(assert (and
  (or (= v (/ 1 100000)) (= v (/ 1 100002)))
  (or (= 0.0 v) (= 0.0 (* (/ 1 10003) v)))))
(set-info :status unsat)
(check-sat)
(exit)
