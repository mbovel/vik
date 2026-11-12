; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/i1321a.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_UFDTLIA)
(declare-fun start!169 () Bool)
(assert start!169)
(assert (=> start!169 false))
(declare-datatypes ((MyInt!35 0))(
  ( (PosInt!22 (v!83 Int)) (MyIntExt!21 (__x!34 Int)) )
))
(declare-fun lt!230 () MyInt!35)
(declare-fun x!468 () MyInt!35)
(assert (=> start!169 (= lt!230 x!468)))
(declare-fun inv!1 (MyInt!35) Bool)
(get-info :version)
(assert (=> start!169 (and (inv!1 x!468) ((_ is PosInt!22) lt!230))))
(declare-fun bs!45 () Bool)
(assert (= bs!45 start!169))
(declare-fun m!675 () Bool)
(assert (=> bs!45 m!675))
(set-info :status unsat)
(check-sat-assuming ((not start!169)))
(set-info :status unsat)
(check-sat)
