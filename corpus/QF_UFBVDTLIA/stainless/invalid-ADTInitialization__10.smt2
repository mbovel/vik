; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/invalid/ADTInitialization.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_UFBVDTLIA)
(declare-fun start!27 () Bool)
(assert start!27)
(assert (=> start!27 false))
(declare-datatypes ((Pair!12 0))(
  ( (PairExt!11 (__x!13 Int) (y!48 (_ BitVec 32)) (x!79 (_ BitVec 32))) (Balanced!12 (x!80 (_ BitVec 32)) (y!49 (_ BitVec 32))) (Imbalanced!12 (x!81 (_ BitVec 32)) (y!50 (_ BitVec 32))) )
))
(declare-fun lt!5 () Pair!12)
(declare-fun thiss!19 () Pair!12)
(assert (=> start!27 (= lt!5 thiss!19)))
(declare-fun inv!2 (Pair!12) Bool)
(get-info :version)
(assert (=> start!27 (and (inv!2 thiss!19) ((_ is Imbalanced!12) lt!5))))
(declare-fun bs!21 () Bool)
(assert (= bs!21 start!27))
(declare-fun m!25 () Bool)
(assert (=> bs!21 m!25))
(set-info :status unsat)
(check-sat-assuming ((not start!27)))
(set-info :status unsat)
(check-sat)
