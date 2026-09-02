; From cvc5: regress0/parser/fresh-declarations.smt2 (modified BSD, LICENSES/cvc5.txt)
; COMMAND-LINE: --no-fresh-declarations
; EXPECT: sat
(set-logic ALL)
(declare-sort U 0)
(declare-fun y () U)
(declare-fun x () Int)
(declare-fun f (Int) Int)
(assert (> (f x) x))
(assert (= y y))
(set-info :status sat)
(check-sat)
