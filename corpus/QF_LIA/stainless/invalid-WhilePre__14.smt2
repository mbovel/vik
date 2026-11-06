; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/invalid/WhilePre.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_LIA)
(declare-fun start!29 () Bool)
(assert start!29)
(declare-fun i!82 () Int)
(declare-fun n!22 () Int)
(declare-fun i!83 () Int)
(assert (=> start!29 (and (= i!82 n!22) (= n!22 5) (> i!83 0) (let ((i!85 (- i!83 1))) (and (> i!85 0) (<= i!85 0))))))
(declare-fun lt!8 () Int)
(assert (=> start!29 (= lt!8 i!82)))
(assert (=> start!29 (= lt!8 n!22)))
(assert (=> start!29 true))
(set-info :status unsat)
(check-sat)
