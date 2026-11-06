; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/RequireEnsuringMessagesStaticChecks.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_UFBVDT)
(declare-fun start!9 () Bool)
(assert start!9)
(declare-datatypes ((MyClass!8 0))(
  ( (MyClass!9 (a!10 (_ BitVec 32))) )
))
(declare-fun mc!1 () MyClass!8)
(assert (=> start!9 (and (bvsgt (a!10 mc!1) #b00000000000000000000000000000000) (bvsle (a!10 mc!1) #b11111111111111111111111111010110))))
(declare-fun inv!0 (MyClass!8) Bool)
(assert (=> start!9 (inv!0 mc!1)))
(declare-fun bs!4 () Bool)
(assert (= bs!4 start!9))
(declare-fun m!5 () Bool)
(assert (=> bs!4 m!5))
(set-info :status unsat)
(check-sat-assuming ((not start!9)))
(set-info :status unsat)
(check-sat)
