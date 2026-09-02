; From cvc5: regress0/parser/issue9568-dt-multi-line.smt2 (modified BSD, LICENSES/cvc5.txt)
; EXPECT: sat
(set-logic ALL)

(declare-datatype Td
  ((T (T_id String)))
)

(set-info :status sat)
(check-sat)
