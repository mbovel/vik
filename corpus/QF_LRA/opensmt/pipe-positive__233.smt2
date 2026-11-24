; From opensmt: pipe/positive/233.smt2 (MIT, LICENSES/opensmt.txt)
(set-option :produce-models true)(set-logic QF_LRA)(declare-fun X () Real)(assert (= 1 X)) (set-info :status sat)
(check-sat) (get-value (X))
