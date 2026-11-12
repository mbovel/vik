; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/AnonymousClasses3.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_UFDTLIA)
(declare-fun start!71 () Bool)
(assert start!71)
(assert (=> start!71 false))
(declare-datatypes ((MyClass!22 0))(
  ( (MyClassExt!21 (__x!23 Int)) ($anon!42) (normal!22) ($anon!43 (something!28 Int)) )
))
(declare-fun lt!20 () MyClass!22)
(declare-fun thiss!64 () MyClass!22)
(assert (=> start!71 (= lt!20 thiss!64)))
(declare-fun inv!1 (MyClass!22) Bool)
(get-info :version)
(assert (=> start!71 (and (inv!1 thiss!64) ((_ is normal!22) lt!20))))
(declare-fun bs!51 () Bool)
(assert (= bs!51 start!71))
(declare-fun m!77 () Bool)
(assert (=> bs!51 m!77))
(set-info :status unsat)
(check-sat-assuming ((not start!71)))
(set-info :status unsat)
(check-sat)
