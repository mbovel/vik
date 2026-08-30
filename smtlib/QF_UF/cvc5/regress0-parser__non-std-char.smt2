; From cvc5: regress0/parser/non-std-char.smt2 (modified BSD, LICENSES/cvc5.txt)
; EXPECT: sat
; DISABLE-TESTER: dump
(set-logic ABVFP)
(set-info :source |cät|)
(set-info :status sat)
(check-sat)
(exit)
