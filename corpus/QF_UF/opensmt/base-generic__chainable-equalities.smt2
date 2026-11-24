; From opensmt: base/generic/chainable-equalities.smt2 (MIT, LICENSES/opensmt.txt)
(set-logic QF_UF)
(declare-fun x () Bool)
(declare-fun y () Bool)
(assert (= x y (= x false)))
(set-info :status unsat)
(check-sat)

