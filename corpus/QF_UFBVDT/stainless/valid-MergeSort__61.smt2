; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/MergeSort.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_UFBVDT)
(declare-fun start!568 () Bool)
(assert start!568)
(declare-datatypes ((List!59 0))(
  ( (Cons!58 (head!286 (_ BitVec 32)) (tail!238 List!59)) (Nil!58) )
))
(declare-datatypes ((LList!18 0))(
  ( (LNil!18) (LCons!18 (head!287 List!59) (tail!239 LList!18)) )
))
(declare-fun llist!9 () LList!18)
(get-info :version)
(assert (=> start!568 false))
(assert (=> start!568 true))
(set-info :status unsat)
(check-sat)
