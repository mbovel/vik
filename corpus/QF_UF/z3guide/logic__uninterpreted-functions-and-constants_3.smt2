; From z3guide: 01 - logic/04 - Uninterpreted-functions-and-constants.md (block 3) (MIT, LICENSES/z3guide.txt)
; statuses: the `unknown` ones answered by Z3 version 4.14.0 (statusstamp, 2026-09-03)
; EXPECT: sat
; EXPECT: ((define-fun x () A @A!0) (define-fun f ((x!0 A)) A (ite (= x!0 @A!0) @A!0 @A!0)))
; EXPECT: unsat
(declare-sort A)
(declare-fun f (A) A)
(declare-const x A)
(assert (= (f (f x)) x))
(assert (= (f (f (f x))) x))
(set-info :status sat)
(check-sat)
(get-model)
  
(assert (not (= (f x) x)))
(set-info :status unsat)
(check-sat)
