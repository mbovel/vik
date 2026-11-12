; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/invalid/PositiveMap.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_UFBVDT)
(declare-fun start!17 () Bool)
(assert start!17)
(declare-datatypes ((List!5 0))(
  ( (Cons!5 (head!18 (_ BitVec 32)) (tail!18 List!5)) (Nil!5) )
))
(declare-fun list!6 () List!5)
(get-info :version)
(assert (=> start!17 false))
(assert (=> start!17 true))
(set-info :status unsat)
(check-sat)
