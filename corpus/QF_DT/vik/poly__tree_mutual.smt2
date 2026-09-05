; Hand-written for VIK (this repository's license): two mutually recursive
; parametric datatypes (the reference's Tree and TreeList), instantiated
; together at Int, and a polymorphic definition over them.
(set-logic ALL)
(declare-sort-parameter X)
(declare-datatypes ((Tree 1) (TreeList 1)) (
  (par (X) ((node (value X) (children (TreeList X)))))
  (par (Y) ((empty) (insert (car (Tree Y)) (cdr (TreeList Y)))))))
(define-fun leaf ((v X)) (Tree X) (node v (as empty (TreeList X))))
(declare-const t (Tree Int))
(declare-const u (Tree Bool))
(assert (= t (node 1 (insert (leaf 2) (as empty (TreeList Int))))))
(assert (= u (leaf false)))
(assert (= (value (car (children t))) 2))
(set-info :status sat)
(check-sat)
(assert (not (= (value u) false)))
(set-info :status unsat)
(check-sat)
