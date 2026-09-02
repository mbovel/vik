; From princess: adt/datatype6b.smt2 (BSD, LICENSES/princess.txt)
; EXPECT: sat
; EXPECT: ((define-fun a () List (cons 0 @List!0)) (define-fun b () List (cons 0 @List!1)))


(declare-datatype List ( (nil) (cons (head Int) (tail List)) ))

(declare-const a List)
(declare-const b List)

(assert (not (= a b)))
(set-info :status sat)
(check-sat)
(get-model)

