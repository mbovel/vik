; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/ChooseWithWitness.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_UFBVDT)
(declare-fun start!94 () Bool)
(assert start!94)
(declare-datatypes ((List!18 0))(
  ( (Nil!17) (Cons!17 (head!82 (_ BitVec 32)) (tail!34 List!18)) )
))
(declare-fun l!107 () List!18)
(get-info :version)
(assert (=> start!94 false))
(assert (=> start!94 true))
(set-info :status unsat)
(check-sat)
