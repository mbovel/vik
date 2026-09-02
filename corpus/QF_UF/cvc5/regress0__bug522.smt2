; From cvc5: regress0/bug522.smt2 (modified BSD, LICENSES/cvc5.txt)
; EXPECT: sat
; EXPECT: sat
(set-option :incremental true)
(set-logic QF_UF)

(push 1)
(declare-sort U 0)
(declare-fun x () U)
(declare-fun y () U)
(assert (= x y))
(set-info :status sat)
(check-sat)
(pop 1)

(set-info :status sat)
(check-sat)
