; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/invalid/ADTInitialization.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_UFBVDTLIA)
(declare-fun start!1 () Bool)
(assert start!1)
(assert (=> start!1 false))
(declare-datatypes ((Pair!2 0))(
  ( (PairExt!1 (__x!3 Int) (y!18 (_ BitVec 32)) (x!45 (_ BitVec 32))) (Balanced!2 (x!46 (_ BitVec 32)) (y!19 (_ BitVec 32))) (Imbalanced!2 (x!47 (_ BitVec 32)) (y!20 (_ BitVec 32))) )
))
(declare-fun thiss!11 () Pair!2)
(declare-fun inv!2 (Pair!2) Bool)
(assert (=> start!1 (inv!2 thiss!11)))
(declare-fun bs!1 () Bool)
(assert (= bs!1 start!1))
(declare-fun m!1 () Bool)
(assert (=> bs!1 m!1))
(set-info :status unsat)
(check-sat-assuming ((not start!1)))
(set-info :status unsat)
(check-sat)
