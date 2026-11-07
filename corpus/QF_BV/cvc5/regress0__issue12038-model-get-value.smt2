; From cvc5: regress0/issue12038-model-get-value.smt2 (modified BSD, LICENSES/cvc5.txt)
; COMMAND-LINE: --produce-models
; EXPECT: sat
; EXPECT: ((A true))
; logic: QF_BV, the smallest the script fits, for ALL (prune logic)
(set-logic QF_BV)
(declare-const A Bool)
(declare-const b (_ BitVec 4))
(assert (or (and A (= b #b0001)) (and A (= b #b0000))))
(set-info :status sat)
(check-sat)
(get-value (A))
