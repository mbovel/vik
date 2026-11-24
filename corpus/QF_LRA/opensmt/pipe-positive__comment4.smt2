; From opensmt: pipe/positive/comment4.smt2 (MIT, LICENSES/opensmt.txt)
(set-option :produce-models true)(set-logic QF_LRA)(declare-fun X () Real)(assert (= 1 X)) (set-info :status sat)
(check-sat) (get-value (X)); comment((asse
(set-info :status sat)
(check-sat) ; Another comment
(set-info :status sat)
(check-sat)
(exit)
