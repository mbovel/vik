; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/Heaps.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_UFBVDTLIA)
(declare-fun start!300 () Bool)
(assert start!300)
(declare-datatypes ((Node!74 0) (Heap!37 0))(
  ( (Node!75 (rank!69 Int) (elem!70 (_ BitVec 32)) (nodes!69 Heap!37)) )
  ( (Empty!37) (Nodes!37 (head!137 Node!74) (tail!89 Heap!37)) )
))
(declare-fun h!151 () Heap!37)
(get-info :version)
(assert (=> start!300 (and (not (= h!151 Empty!37)) (or (not ((_ is Nodes!37) h!151)) (not ((_ is Empty!37) (tail!89 h!151)))) (not ((_ is Nodes!37) h!151)))))
(assert (=> start!300 true))
(set-info :status unsat)
(check-sat)
