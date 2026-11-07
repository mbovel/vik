; From princess: adt/datatype.smt2 (BSD, LICENSES/princess.txt)
; logic: QF_DT, the smallest the script fits, which set none (prune logic)
(set-logic QF_DT)

(declare-datatype Color ( (red) (green) (blue) ))

(declare-fun x () Color)
(declare-fun y () Color)

(assert (= red green))
(set-info :status unsat)
(check-sat)
