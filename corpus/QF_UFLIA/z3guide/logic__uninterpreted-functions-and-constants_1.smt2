; From z3guide: 01 - logic/04 - Uninterpreted-functions-and-constants.md (block 1) (MIT, LICENSES/z3guide.txt)
; statuses: the `unknown` ones answered by Z3 version 4.14.0 (statusstamp, 2026-09-03)
; EXPECT: sat
; EXPECT: ((define-fun a () Int 1) (define-fun b () Int 0) (define-fun f ((x!0 Int)) Int (ite (= x!0 10) 1 1)))
(declare-fun f (Int) Int)
(declare-fun a () Int) ; a is a constant
(declare-const b Int) ; syntax sugar for (declare-fun b () Int)
(assert (< a 20))
(assert (< b a))
(assert (= (f 10) 1))
(set-info :status sat)
(check-sat)
(get-model)
