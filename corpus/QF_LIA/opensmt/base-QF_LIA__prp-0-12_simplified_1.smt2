; From opensmt: base/QF_LIA/prp-0-12_simplified_1.smt2 (MIT, LICENSES/opensmt.txt)
(set-logic QF_LIA)
(declare-fun v () Int)
(set-info :status sat)
(assert
 (=
  (ite
   (= (ite (= v 0) 1 0) 0)
   (ite (= v 0) 1 0)
   (ite (= (ite (= v 0) 1 0) 0) 1 0))
  0
))
(set-info :status sat)
(check-sat)
(exit)
