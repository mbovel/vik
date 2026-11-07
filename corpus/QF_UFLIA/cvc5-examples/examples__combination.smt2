; From cvc5-examples: combination.smt2 (modified BSD, LICENSES/cvc5.txt)
; statuses: the `unknown` ones answered by This is cvc5 version 1.2.1 (statusstamp, 2026-09-03)
; EXPECT: unsat
; EXPECT: sat
; EXPECT: (((f x) 0) ((f y) 1) ((+ (f x) (f y)) 1) ((p 0) false) ((p (f y)) true))
; logic: QF_UFLIA, the smallest the script fits, for QF_UFLIRA (prune logic)
(set-logic QF_UFLIA)
(set-option :produce-models true)
(set-option :incremental true)

(declare-sort U 0)

(declare-const x U)
(declare-const y U)

(declare-fun f (U) Int)
(declare-fun p (Int) Bool)

; 0 <= f(x)
(assert (<= 0 (f x)))
; 0 <= f(y)
(assert (<= 0 (f y)))
; f(x) + f(y) <= 1
(assert (<= (+ (f x) (f y)) 1))
; not p(0)
(assert (not (p 0)))
; p(f(y))
(assert (p (f y)))

(echo "Prove x != y is entailed. UNSAT (of negation) == ENTAILED")
(set-info :status unsat)
(check-sat-assuming ((not (distinct x y))))

(echo "Call checkSat to show that the assertions are satisfiable")
(set-info :status sat)
(check-sat)

(echo "Call (getValue (...)) on terms of interest")
(get-value ((f x) (f y) (+ (f x) (f y)) (p 0) (p (f y))))
