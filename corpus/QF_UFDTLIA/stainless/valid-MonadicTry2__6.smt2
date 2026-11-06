; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/MonadicTry2.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_UFDTLIA)
(declare-fun start!21 () Bool)
(assert start!21)
(declare-datatypes ((Exception!9 0))(
  ( (ExceptionExt!7 (__x!9 Int)) )
))
(declare-datatypes ((Try!8 0))(
  ( (Failure!8 (exc!20 Exception!9)) (Success!8 (t!21 Int)) )
))
(declare-fun lt!47 () Try!8)
(declare-fun i!6 () Int)
(declare-fun checkPositive!0 (Int) Try!8)
(assert (=> start!21 (= lt!47 (checkPositive!0 i!6))))
(assert (=> start!21 false))
(assert (=> start!21 true))
(declare-fun bs!7 () Bool)
(assert (= bs!7 start!21))
(declare-fun m!33 () Bool)
(assert (=> bs!7 m!33))
(set-info :status unsat)
(check-sat-assuming ((not start!21)))
(set-info :status unsat)
(check-sat)
