; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/Heaps.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_UFBVDTLIA)
(declare-fun start!258 () Bool)
(assert start!258)
(declare-datatypes ((Node!58 0) (Heap!29 0))(
  ( (Node!59 (rank!61 Int) (elem!62 (_ BitVec 32)) (nodes!61 Heap!29)) )
  ( (Empty!29) (Nodes!29 (head!129 Node!58) (tail!81 Heap!29)) )
))
(declare-fun h!151 () Heap!29)
(get-info :version)
(assert (=> start!258 (and (not (= h!151 Empty!29)) (or (not ((_ is Nodes!29) h!151)) (not ((_ is Empty!29) (tail!81 h!151)))) ((_ is Nodes!29) h!151) (= (tail!81 h!151) Empty!29))))
(assert (=> start!258 true))
(set-info :status unsat)
(check-sat)
