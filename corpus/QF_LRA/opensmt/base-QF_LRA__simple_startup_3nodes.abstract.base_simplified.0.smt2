; From opensmt: base/QF_LRA/simple_startup_3nodes.abstract.base_simplified.0.smt2 (MIT, LICENSES/opensmt.txt)
(set-logic QF_LRA)
(declare-fun _substvar_1_ () Real)
(assert (let (
  (?v_39 (+ (+ _substvar_1_ (* (ite true (- 0.0 1) 0.0) 3)) 0.0))
 )
 (let (
  (?v_178 (and false (or false (and false (or false (not (<= _substvar_1_ ?v_39)))))))
 )

 false)))
(set-info :status unsat)
(check-sat)
(exit)
