; From princess: smtlib-parser/for_in_term2.smt2 (BSD, LICENSES/princess.txt)
; logic: QF_UFLIA, the smallest the script fits, for AUFLIA (prune logic)

(set-logic QF_UFLIA)

(declare-fun a () Int)
(declare-fun b () Int)

(declare-fun f (Bool) Int)

(assert (= (f (= a b)) a))
(assert (= a 5))

(set-option :inline-let false)

(assert (not (=> (= b 6) (let ((x false)) (= (f x) 5)))))

(set-info :status unsat)
(check-sat)
