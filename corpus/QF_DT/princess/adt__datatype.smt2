; From princess: adt/datatype.smt2 (BSD, LICENSES/princess.txt)

(declare-datatype Color ( (red) (green) (blue) ))

(declare-fun x () Color)
(declare-fun y () Color)

(assert (= red green))
(set-info :status unsat)
(check-sat)