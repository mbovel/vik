; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/invalid/ADTInitialization.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_UFBVDTLIA)
(declare-fun start!37 () Bool)
(assert start!37)
(declare-datatypes ((Pair!17 0))(
  ( (PairExt!16 (__x!18 Int) (y!63 (_ BitVec 32)) (x!94 (_ BitVec 32))) (Balanced!17 (x!95 (_ BitVec 32)) (y!64 (_ BitVec 32))) (Imbalanced!17 (x!96 (_ BitVec 32)) (y!65 (_ BitVec 32))) )
))
(declare-fun thiss!19 () Pair!17)
(get-info :version)
(assert (=> start!37 (and ((_ is Imbalanced!17) thiss!19) (bvsle #b00000000000000000000000000000000 (x!96 thiss!19)) (bvsle (x!96 thiss!19) #b00000000000000000000000001100100) (bvsle #b00000000000000000000000000000000 (y!65 thiss!19)) (bvsle (y!65 thiss!19) #b00000000000000000000000001100100) (= (bvand (x!96 thiss!19) #b10000000000000000000000000000000) (bvand (y!65 thiss!19) #b10000000000000000000000000000000)) (not (= (bvand (x!96 thiss!19) #b10000000000000000000000000000000) (bvand (bvadd (x!96 thiss!19) (y!65 thiss!19)) #b10000000000000000000000000000000))))))
(declare-fun lt!20 () Pair!17)
(assert (=> start!37 (= lt!20 thiss!19)))
(declare-fun inv!2 (Pair!17) Bool)
(assert (=> start!37 (and (inv!2 thiss!19) ((_ is Imbalanced!17) lt!20))))
(declare-fun bs!31 () Bool)
(assert (= bs!31 start!37))
(declare-fun m!35 () Bool)
(assert (=> bs!31 m!35))
(set-info :status unsat)
(check-sat-assuming ((not start!37)))
(set-info :status unsat)
(check-sat)
