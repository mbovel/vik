; From cvc5: regress0/parser/version2.7-sort-param.smt2 (modified BSD, LICENSES/cvc5.txt)
; COMMAND-LINE: -q
; EXPECT: sat
; logic: QF_UF, the smallest the script fits, for ALL (prune logic)
(set-info :smt-lib-version 2.7)
(set-logic QF_UF)
(declare-sort-parameter foo)
(set-info :status sat)
(check-sat)
