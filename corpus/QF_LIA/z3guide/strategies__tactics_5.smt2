; From z3guide: 03 - strategies/03 - tactics.md (block 5) (MIT, LICENSES/z3guide.txt)
; statuses: the `unknown` ones answered by Z3 version 4.14.0 (statusstamp, 2026-09-03)
; EXPECT: sat
; EXPECT: ((define-fun x () Int 0) (define-fun y () Int (- 2)))
(declare-const x Int)
(declare-const y Int)

(assert (> x (+ y 1)))

(set-info :status sat)
(check-sat-using smt)
(get-model)
