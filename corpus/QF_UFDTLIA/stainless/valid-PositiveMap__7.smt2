; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/PositiveMap.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_UFDTLIA)
(declare-fun start!35 () Bool)
(assert start!35)
(declare-datatypes ((List!9 0))(
  ( (Cons!9 (head!29 Int) (tail!29 List!9)) (Nil!9) )
))
(declare-fun list!7 () List!9)
(get-info :version)
(assert (=> start!35 false))
(assert (=> start!35 true))
(set-info :status unsat)
(check-sat)
