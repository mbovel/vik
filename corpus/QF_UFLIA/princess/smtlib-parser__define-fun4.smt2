; From princess: smtlib-parser/define-fun4.smt2 (BSD, LICENSES/princess.txt)
; EXPECT: sat
; EXPECT: ((define-fun y () Int 0) (define-fun f ((x!0 Int)) Int (ite (= x!0 42) 1 1)))
; logic: QF_UFLIA, the smallest the script fits, which set none (prune logic)
(set-logic QF_UFLIA)

(set-option :inline-size-limit 0)

(declare-const y Int)
(declare-fun f (Int) Int)
(define-fun inc ((x Int)) Int (+ x 1))

(assert (= (f 42) (inc y)))

(set-info :status sat)
(check-sat)
(get-model)

