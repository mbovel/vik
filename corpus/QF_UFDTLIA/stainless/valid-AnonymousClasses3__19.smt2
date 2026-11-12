; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/AnonymousClasses3.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_UFDTLIA)
(declare-fun start!69 () Bool)
(assert start!69)
(assert (=> start!69 false))
(declare-datatypes ((MyClass!21 0))(
  ( (MyClassExt!20 (__x!22 Int)) ($anon!40) (normal!21) ($anon!41 (something!27 Int)) )
))
(declare-fun lt!17 () MyClass!21)
(declare-fun thiss!21 () MyClass!21)
(assert (=> start!69 (= lt!17 thiss!21)))
(declare-fun inv!1 (MyClass!21) Bool)
(get-info :version)
(assert (=> start!69 (and (inv!1 thiss!21) ((_ is $anon!40) lt!17))))
(declare-fun bs!49 () Bool)
(assert (= bs!49 start!69))
(declare-fun m!75 () Bool)
(assert (=> bs!49 m!75))
(set-info :status unsat)
(check-sat-assuming ((not start!69)))
(set-info :status unsat)
(check-sat)
