; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/invalid/Equations3.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_UFDTLIA)
(declare-fun start!3 () Bool)
(assert start!3)
(declare-datatypes ((Unit!4 0))(
  ( (Unit!5) )
))
(declare-fun lt!5 () Unit!4)
(declare-fun Unit!6 () Unit!4)
(assert (=> start!3 (= lt!5 Unit!6)))
(declare-fun x!38 () Int)
(declare-fun z!1 () Int)
(assert (=> start!3 (= x!38 z!1)))
(declare-fun y!17 () Int)
(assert (=> start!3 (and (= x!38 z!1) (not (= z!1 y!17)))))
(assert (=> start!3 true))
(set-info :status sat)
(check-sat)
(get-model)
