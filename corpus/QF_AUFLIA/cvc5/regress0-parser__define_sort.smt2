; From cvc5: regress0/parser/define_sort.smt2 (modified BSD, LICENSES/cvc5.txt)
; EXPECT: sat
; logic: QF_AUFLIA, the smallest the script fits, for ALIA (prune logic)
(set-logic QF_AUFLIA)
(define-sort Stream (T) (Array Int T))
(define-sort IntStream () (Stream Int))
(set-info :status sat)
(check-sat)
