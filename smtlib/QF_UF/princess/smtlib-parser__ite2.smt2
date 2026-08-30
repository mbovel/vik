; From princess: smtlib-parser/ite2.smt2 (BSD, LICENSES/princess.txt)

(set-logic AUFLIA)

(declare-fun a () Bool)
(declare-fun b () Bool)

(assert (not (= (= a b) (ite a b (not b)))))

(set-info :status unsat)
(check-sat)
