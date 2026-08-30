; From princess: adt/datatype6b.smt2 (BSD, LICENSES/princess.txt)


(declare-datatype List ( (nil) (cons (head Int) (tail List)) ))

(declare-const a List)
(declare-const b List)

(assert (not (= a b)))
(set-info :status sat)
(check-sat)
(get-model)

