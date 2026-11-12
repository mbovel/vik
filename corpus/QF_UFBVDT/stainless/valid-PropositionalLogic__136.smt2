; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/PropositionalLogic.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_UFBVDT)
(declare-fun start!837 () Bool)
(assert start!837)
(declare-datatypes ((Formula!134 0))(
  ( (Literal!134 (id!156 (_ BitVec 32))) (Not!134 (f!591 Formula!134)) (And!134 (lhs!710 Formula!134) (rhs!710 Formula!134)) (Implies!134 (lhs!711 Formula!134) (rhs!711 Formula!134)) (Or!134 (lhs!712 Formula!134) (rhs!712 Formula!134)) )
))
(declare-fun thiss!9 () Formula!134)
(get-info :version)
(assert (=> start!837 false))
(assert (=> start!837 true))
(set-info :status unsat)
(check-sat)
