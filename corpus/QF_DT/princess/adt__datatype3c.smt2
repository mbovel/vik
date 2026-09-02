; From princess: adt/datatype3c.smt2 (BSD, LICENSES/princess.txt)
; EXPECT: sat
; EXPECT: sat
; EXPECT: (((data (head (children x))) 42))
; EXPECT: unsat
; EXPECT: unsat
; EXPECT: sat
; EXPECT: ((y2 42))
; EXPECT: unsat

(declare-datatypes ((Tree 0) (TreeList 0)) (
   ((node (data Int) (children TreeList)))
   ((nil) (cons (head Tree) (tail TreeList)))))

(define-fun leaf ((x Int)) Tree (node x nil))

(declare-fun x () Tree)
(declare-fun y () Int)
(declare-fun y2 () Int)
(declare-fun y3 () Int)
(declare-fun z () Tree)

(push 1)
(assert (= x (node 1 (cons z nil))))
(set-info :status sat)
(check-sat)
(assert (= z (leaf 42)))
(set-info :status sat)
(check-sat)
(get-value ((data (head (children x)))))
(push 1)
(assert (< (data (head (children x))) 0))
(set-info :status unsat)
(check-sat)
(pop 1)
(push 1)
(assert (= x (node y (cons (leaf y2) (cons (leaf y3) nil)))))
(set-info :status unsat)
(check-sat)
(pop 1)
(push 1)
(assert (= x (node y (cons (leaf y2) nil))))
(set-info :status sat)
(check-sat)
(get-value (y2))
(pop 1)
(pop 1)

(set-option :produce-interpolants true)

(push 1)
(assert (! (= x (node 1 (cons (leaf y) (cons (leaf y2) nil)))) :named left))
(assert (! (= x (node 1 (cons (leaf y) nil))) :named right))
(set-info :status unsat)
(check-sat)
(get-interpolants left right)
(pop 1)
