; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/invalid/WhilePre.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_LIA)
(declare-fun start!37 () Bool)
(assert start!37)
(declare-fun i!82 () Int)
(declare-fun n!22 () Int)
(declare-fun i!83 () Int)
(assert (=> start!37 (and (= i!82 n!22) (= n!22 5) (> i!83 0) (< i!83 0))))
(declare-fun lt!20 () Int)
(assert (=> start!37 (= lt!20 i!82)))
(assert (=> start!37 (= lt!20 n!22)))
(assert (=> start!37 true))
(set-info :status unsat)
(check-sat)
