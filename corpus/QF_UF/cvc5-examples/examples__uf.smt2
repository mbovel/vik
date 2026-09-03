; From cvc5-examples: uf.smt2 (modified BSD, LICENSES/cvc5.txt)
; statuses: the `unknown` ones answered by Z3 version 4.14.0 (statusstamp, 2026-09-03)
(set-logic QF_UF)

(declare-sort U 0)

(declare-const x U)
(declare-const y U)

(declare-fun f (U) U)
(declare-fun p (U) Bool)

(assert (= x (f x)))
(assert (= y (f y)))
(assert (not (p (f x))))
(assert (p (f y)))

(set-info :status sat)
(check-sat)

