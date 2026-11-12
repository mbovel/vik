; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/AnonymousClasses3.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_UFDTLIA)
(declare-fun start!51 () Bool)
(assert start!51)
(assert (=> start!51 false))
(declare-datatypes ((MyClass!12 0))(
  ( (MyClassExt!11 (__x!13 Int)) ($anon!22) (normal!12) ($anon!23 (something!18 Int)) )
))
(declare-fun lt!2 () MyClass!12)
(declare-fun thiss!32 () MyClass!12)
(assert (=> start!51 (= lt!2 thiss!32)))
(declare-fun inv!1 (MyClass!12) Bool)
(get-info :version)
(assert (=> start!51 (and (inv!1 thiss!32) ((_ is MyClassExt!11) lt!2))))
(declare-fun bs!31 () Bool)
(assert (= bs!31 start!51))
(declare-fun m!57 () Bool)
(assert (=> bs!31 m!57))
(set-info :status unsat)
(check-sat-assuming ((not start!51)))
(set-info :status unsat)
(check-sat)
