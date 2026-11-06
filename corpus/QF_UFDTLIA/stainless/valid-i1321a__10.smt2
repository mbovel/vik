; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/i1321a.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_UFDTLIA)
(declare-fun start!95 () Bool)
(assert start!95)
(assert (=> start!95 false))
(declare-datatypes ((MyInt!20 0))(
  ( (PosInt!11 (v!72 Int)) (MyIntExt!10 (__x!23 Int)) )
))
(declare-fun thiss!39 () MyInt!20)
(declare-fun inv!1 (MyInt!20) Bool)
(assert (=> start!95 (inv!1 thiss!39)))
(declare-fun bs!18 () Bool)
(assert (= bs!18 start!95))
(declare-fun m!405 () Bool)
(assert (=> bs!18 m!405))
(set-info :status unsat)
(check-sat-assuming ((not start!95)))
(set-info :status unsat)
(check-sat)
