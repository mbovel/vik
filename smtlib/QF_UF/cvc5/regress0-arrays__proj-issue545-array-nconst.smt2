; From cvc5: regress0/arrays/proj-issue545-array-nconst.smt2 (modified BSD, LICENSES/cvc5.txt)
; SCRUBBER: sed -e 's/((f.*/((f/g'
; EXPECT: sat
; EXPECT: ((f
(set-logic ALL)
(set-option :produce-models true)
(declare-sort u 0)
(declare-const x u)
(define-fun f ((_f5_1 u) (_f5_2 Bool)) u (ite _f5_2 _f5_1 x))
(set-info :status sat)
(check-sat)
(get-value (f))
