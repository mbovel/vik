; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/invalid/RedBlackTree2.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_UFBVDT)
(declare-fun start!156 () Bool)
(assert start!156)
(declare-datatypes ((Color!14 0))(
  ( (Red!14) (Black!14) )
))
(declare-datatypes ((Tree!14 0))(
  ( (Empty!14) (Node!14 (color!45 Color!14) (left!47 Tree!14) (value!90 (_ BitVec 32)) (right!47 Tree!14)) )
))
(declare-fun t!85 () Tree!14)
(get-info :version)
(assert (=> start!156 false))
(assert (=> start!156 true))
(set-info :status unsat)
(check-sat)
