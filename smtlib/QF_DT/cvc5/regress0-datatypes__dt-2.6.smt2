; From cvc5: regress0/datatypes/dt-2.6.smt2 (modified BSD, LICENSES/cvc5.txt)
; COMMAND-LINE: --lang=smt2.6
; EXPECT: sat
(set-logic ALL)
(set-info :status sat)
(declare-datatypes ((IntList 0)) (
((empty) (set.insert ( head Int ) ( tail IntList ) ))
))

(declare-fun x () IntList)
(declare-fun y () IntList)
(declare-fun z () IntList)

(assert (distinct x y z))

(set-info :status sat)
(check-sat)
