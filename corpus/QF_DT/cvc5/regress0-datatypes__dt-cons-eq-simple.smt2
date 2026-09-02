; From cvc5: regress0/datatypes/dt-cons-eq-simple.smt2 (modified BSD, LICENSES/cvc5.txt)
; EXPECT: unsat
(set-logic ALL)

(declare-datatype List ((cons (head Int) (tail List)) (nil)))

(declare-fun x () Int)
(declare-fun y () Int)

(assert (not (= x y)))
(assert (= (cons x nil) (cons y nil)))
(set-info :status unsat)
(check-sat)
