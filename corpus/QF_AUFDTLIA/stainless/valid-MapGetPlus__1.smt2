; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/MapGetPlus.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_AUFDTLIA)
(declare-fun start!3 () Bool)
(assert start!3)
(declare-datatypes ((Option!7 0))(
  ( (None!6) (Some!6 (v!28 Int)) )
))
(declare-fun a!2 () (Array Int Option!7))
(get-info :version)
(assert (=> start!3 (and (= (select a!2 0) None!6) (let ((b!2 (store a!2 0 (Some!6 1)))) (and ((_ is Some!6) (select b!2 0)) (not ((_ is Some!6) (select b!2 0))))))))
(assert (=> start!3 true))
(declare-fun bs!3 () Bool)
(assert (= bs!3 start!3))
(declare-fun m!7 () Bool)
(assert (=> bs!3 m!7))
(declare-fun m!9 () Bool)
(assert (=> bs!3 m!9))
(declare-fun m!11 () Bool)
(assert (=> bs!3 m!11))
(set-info :status unsat)
(check-sat)
