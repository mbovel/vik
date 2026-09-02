; From cvc5: regress0/parser/no-fresh.smt2 (modified BSD, LICENSES/cvc5.txt)
; COMMAND-LINE: --no-fresh-declarations
; EXIT: 0
; EXPECT: sat
; DISABLE-TESTER: dump
(set-logic ALL)
(declare-fun x () Int)
(declare-fun x () Int)
(set-info :status sat)
(check-sat)
