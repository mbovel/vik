; From cvc5: regress0/arith/issue8159-rewrite-intreal.smt2 (modified BSD, LICENSES/cvc5.txt)
; EXPECT: sat
(set-logic QF_LIRA)
(declare-fun a () Real)
(declare-fun b () Int)
(assert (>= a (+ a (to_real b))))
(set-info :status sat)
(check-sat)
