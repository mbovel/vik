; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/NaturalDeduction.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_UF)
(declare-fun start!19 () Bool)
(assert start!19)
(declare-fun p!57 () Bool)
(declare-fun q!19 () Bool)
(assert (=> start!19 (and (or (not p!57) q!19) (not q!19) p!57)))
(assert (=> start!19 true))
(set-info :status unsat)
(check-sat)
