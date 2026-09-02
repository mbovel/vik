; From cvc5: regress0/parser/define_sort.smt2 (modified BSD, LICENSES/cvc5.txt)
; EXPECT: sat
(set-logic ALIA)
(define-sort Stream (T) (Array Int T))
(define-sort IntStream () (Stream Int))
(set-info :status sat)
(check-sat)
