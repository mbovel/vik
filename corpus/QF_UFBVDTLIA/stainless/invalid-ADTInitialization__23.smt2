; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/invalid/ADTInitialization.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_UFBVDTLIA)
(declare-fun start!59 () Bool)
(assert start!59)
(declare-datatypes ((Pair!25 0))(
  ( (PairExt!24 (__x!26 Int) (y!87 (_ BitVec 32)) (x!122 (_ BitVec 32))) (Balanced!25 (x!123 (_ BitVec 32)) (y!88 (_ BitVec 32))) (Imbalanced!25 (x!124 (_ BitVec 32)) (y!89 (_ BitVec 32))) )
))
(declare-fun thiss!18 () Pair!25)
(get-info :version)
(assert (=> start!59 (and ((_ is Balanced!25) thiss!18) (bvsle #b00000000000000000000000000000000 (x!123 thiss!18)) (bvsle (x!123 thiss!18) #b00000000000000000000000001100100) (bvsle #b00000000000000000000000000000000 (y!88 thiss!18)) (bvsle (y!88 thiss!18) #b00000000000000000000000001100100) (= (bvand (x!123 thiss!18) #b10000000000000000000000000000000) (bvand (y!88 thiss!18) #b10000000000000000000000000000000)) (not (= (bvand (x!123 thiss!18) #b10000000000000000000000000000000) (bvand (bvadd (x!123 thiss!18) (y!88 thiss!18)) #b10000000000000000000000000000000))))))
(declare-fun lt!41 () Pair!25)
(assert (=> start!59 (= lt!41 thiss!18)))
(declare-fun inv!2 (Pair!25) Bool)
(assert (=> start!59 (and (inv!2 thiss!18) ((_ is Balanced!25) lt!41))))
(declare-fun bs!47 () Bool)
(assert (= bs!47 start!59))
(declare-fun m!55 () Bool)
(assert (=> bs!47 m!55))
(set-info :status unsat)
(check-sat-assuming ((not start!59)))
(set-info :status unsat)
(check-sat)
