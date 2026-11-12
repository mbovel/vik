; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/invalid/ADTInitialization.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_UFBVDTLIA)
(declare-fun start!47 () Bool)
(assert start!47)
(assert (=> start!47 false))
(declare-datatypes ((Pair!19 0))(
  ( (PairExt!18 (__x!20 Int) (y!69 (_ BitVec 32)) (x!104 (_ BitVec 32))) (Balanced!19 (x!105 (_ BitVec 32)) (y!70 (_ BitVec 32))) (Imbalanced!19 (x!106 (_ BitVec 32)) (y!71 (_ BitVec 32))) )
))
(declare-fun lt!23 () Pair!19)
(declare-fun thiss!18 () Pair!19)
(assert (=> start!47 (= lt!23 thiss!18)))
(declare-fun inv!2 (Pair!19) Bool)
(get-info :version)
(assert (=> start!47 (and (inv!2 thiss!18) ((_ is Balanced!19) lt!23))))
(declare-fun bs!35 () Bool)
(assert (= bs!35 start!47))
(declare-fun m!43 () Bool)
(assert (=> bs!35 m!43))
(set-info :status unsat)
(check-sat-assuming ((not start!47)))
(set-info :status unsat)
(check-sat)
