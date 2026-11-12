; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/Heaps.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_UFBVDTLIA)
(declare-fun start!232 () Bool)
(assert start!232)
(declare-datatypes ((Node!50 0) (Heap!25 0))(
  ( (Node!51 (rank!57 Int) (elem!58 (_ BitVec 32)) (nodes!57 Heap!25)) )
  ( (Empty!25) (Nodes!25 (head!125 Node!50) (tail!77 Heap!25)) )
))
(declare-fun h!287 () Heap!25)
(get-info :version)
(assert (=> start!232 (and (not ((_ is Empty!25) h!287)) (= h!287 Empty!25))))
(assert (=> start!232 true))
(set-info :status unsat)
(check-sat)
