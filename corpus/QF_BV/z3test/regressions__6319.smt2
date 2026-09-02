; From z3test: 6319.smt2 (MIT, LICENSES/z3test.txt)
(set-option :sat.euf true)
(set-option :tactic.default_tactic smt)
(assert (distinct (_ bv1 1) (_ bv0 1)))
(set-info :status sat)
(check-sat)
(reset)


(set-option :sat.euf true)
(set-option :tactic.default_tactic smt)
(assert (distinct false true))
(set-info :status sat)
(check-sat)