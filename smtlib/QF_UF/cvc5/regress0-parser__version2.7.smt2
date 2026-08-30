; From cvc5: regress0/parser/version2.7.smt2 (modified BSD, LICENSES/cvc5.txt)
; COMMAND-LINE: -q
; EXPECT: sat
(set-info :smt-lib-version 2.7)
(set-logic ALL)
(set-info :status sat)
(check-sat)
