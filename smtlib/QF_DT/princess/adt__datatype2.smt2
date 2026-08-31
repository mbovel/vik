; From princess: adt/datatype2.smt2 (BSD, LICENSES/princess.txt)
; EXPECT: unsat
; EXPECT: unsat
; EXPECT: sat
; EXPECT: ((y 2) (y2 1))
; EXPECT: sat
; EXPECT: unsat

(declare-datatype List ( (nil) (cons (head Int) (tail List)) ))

(declare-fun x () List)
(declare-fun y () Int)
(declare-fun y2 () Int)
(declare-fun z () List)

(push 1)
(assert (= x (cons 1 (cons 2 nil))))
(assert (< (head x) 0))
(set-info :status unsat)
(check-sat)
(pop 1)

(push 1)
(assert (not (= x nil)))
(assert (not (= x (cons (head x) (tail x)))))
(set-info :status unsat)
(check-sat)
(pop 1)

(push 1)
(assert (= (cons 1 (cons y nil)) (cons y2 (cons 2 nil))))
(set-info :status sat)
(check-sat)
(get-value (y y2))
(pop 1)

(push 1)
(assert (= (cons 1 (cons 2 z)) (cons 1 x)))
(set-info :status sat)
(check-sat)
(assert (= x z))
(set-info :status unsat)
(check-sat)
(pop 1)
