; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/CovCollection.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_UFDTLIA)
(declare-fun start!478 () Bool)
(assert start!478)
(assert (=> start!478 false))
(declare-datatypes ((UnsealedSeal!72 0))(
  ( (EmpressLeakySeal!37 (x!15836 Int) (y!183 Int)) (EmperorLeakySeal!37 (x!15837 Int) (y!184 Int)) (UnsealedSealExt!36 (__x!71 Int)) )
))
(declare-fun lt!2938 () UnsealedSeal!72)
(declare-fun thiss!53 () UnsealedSeal!72)
(assert (=> start!478 (= lt!2938 thiss!53)))
(get-info :version)
(assert (=> start!478 ((_ is UnsealedSealExt!36) lt!2938)))
(set-info :status unsat)
(check-sat)
