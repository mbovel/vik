; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/AnonymousClasses3.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_UFDTLIA)
(declare-fun start!61 () Bool)
(assert start!61)
(assert (=> start!61 false))
(declare-datatypes ((MyClass!17 0))(
  ( (MyClassExt!16 (__x!18 Int)) ($anon!32) (normal!17) ($anon!33 (something!23 Int)) )
))
(declare-fun lt!5 () MyClass!17)
(declare-fun thiss!27 () MyClass!17)
(assert (=> start!61 (= lt!5 thiss!27)))
(declare-fun inv!1 (MyClass!17) Bool)
(get-info :version)
(assert (=> start!61 (and (inv!1 thiss!27) ((_ is $anon!33) lt!5))))
(declare-fun bs!41 () Bool)
(assert (= bs!41 start!61))
(declare-fun m!67 () Bool)
(assert (=> bs!41 m!67))
(set-info :status unsat)
(check-sat-assuming ((not start!61)))
(set-info :status unsat)
(check-sat)
