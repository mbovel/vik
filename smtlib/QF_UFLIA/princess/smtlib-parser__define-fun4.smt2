; From princess: smtlib-parser/define-fun4.smt2 (BSD, LICENSES/princess.txt)

(set-option :inline-size-limit 0)

(declare-const y Int)
(declare-fun f (Int) Int)
(define-fun inc ((x Int)) Int (+ x 1))

(assert (= (f 42) (inc y)))

(set-info :status sat)
(check-sat)
(get-model)

