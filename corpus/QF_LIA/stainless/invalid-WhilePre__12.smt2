; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/invalid/WhilePre.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_LIA)
(declare-fun start!25 () Bool)
(assert start!25)
(assert (=> start!25 false))
(declare-fun lt!2 () Int)
(declare-fun i!82 () Int)
(assert (=> start!25 (= lt!2 i!82)))
(declare-fun n!22 () Int)
(assert (=> start!25 (= lt!2 n!22)))
(assert (=> start!25 true))
(set-info :status unsat)
(check-sat)
