; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/NothingTypeEncoding.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_BV)
(declare-fun start!1 () Bool)
(assert start!1)
(declare-fun arg!4 () (_ BitVec 32))
(declare-fun targetBound!6 () Bool)
(assert (=> start!1 (and (bvsgt arg!4 #b00000000000000000000000000000000) (= targetBound!6 (= arg!4 #b00000000000000000000000000000000)) targetBound!6)))
(assert (=> start!1 true))
(declare-fun lt!2 () Bool)
(assert (=> start!1 (= lt!2 targetBound!6)))
(assert (=> start!1 (= lt!2 (= arg!4 #b00000000000000000000000000000000))))
(set-info :status unsat)
(check-sat)
