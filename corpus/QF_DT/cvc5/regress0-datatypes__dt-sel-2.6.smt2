; From cvc5: regress0/datatypes/dt-sel-2.6.smt2 (modified BSD, LICENSES/cvc5.txt)
; COMMAND-LINE: --lang=smt2.6
; EXPECT: unsat

(set-logic ALL)
(set-info :status unsat)
(declare-datatypes ((IntList 0)) (
((empty) (set.insert ( head Int ) ( tail IntList ) ))
))

(declare-fun x () IntList)
(declare-fun y () IntList)
(declare-fun z () IntList)

(assert (distinct x y z))

(assert (not ((_ is set.insert) x)))
(assert (not ((_ is set.insert) y)))

(set-info :status unsat)
(check-sat)
