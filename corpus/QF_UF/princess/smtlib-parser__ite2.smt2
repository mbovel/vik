; From princess: smtlib-parser/ite2.smt2 (BSD, LICENSES/princess.txt)
; logic: QF_UF, the smallest the script fits, for AUFLIA (prune logic)

(set-logic QF_UF)

(declare-fun a () Bool)
(declare-fun b () Bool)

(assert (not (= (= a b) (ite a b (not b)))))

(set-info :status unsat)
(check-sat)
