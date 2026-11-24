; From opensmt: base/QF_LRA/clocksynchro_2clocks.worst_case_skew.induct_simplified_0.smt2 (MIT, LICENSES/opensmt.txt)
(set-logic QF_LRA)
(declare-fun v2 () Real)
(assert
 (<= 0.0
     (- v2
        (+ (+ 0.0
              (* (* (+ v2 2) 1001)
                 (/ 1 999)))
              0.0))))
(set-info :status sat)
(check-sat)
(exit)
