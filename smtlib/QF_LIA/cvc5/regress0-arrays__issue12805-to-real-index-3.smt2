; From cvc5: regress0/arrays/issue12805-to-real-index-3.smt2 (modified BSD, LICENSES/cvc5.txt)
; COMMAND-LINE: --check-models
; EXPECT: sat
(set-logic ALL)
(declare-const p Bool)
(declare-const i1 Int)
(declare-const i2 Int)
(declare-const a0 (Array Real Int))
(declare-const a2 (Array Real Int))
(assert (xor p (not (= a2 (store a0 (to_real i2) (- 2))))))
(assert (= (select a0 2.0) (select a0 (to_real i1))))
(set-info :status sat)
(check-sat)
