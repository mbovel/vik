; From cvc5: regress0/proofs/dd_int_check_bvnot_lam_value_norm.smt2 (modified BSD, LICENSES/cvc5.txt)
; DISABLE-TESTER: alethe
; EXPECT: unsat
; logic: QF_LIA, the smallest the script fits, for ALL (prune logic)
(set-logic QF_LIA)
(define-fun l ((s Int) (x Int) (t Int) (x0 Int)) Bool (= x 0))
(define-fun n ((s Int) (t Int) (x0 Int)) Bool (and false (l 0 0 0 0)))
(assert (n 0 0 0))
(set-info :status unsat)
(check-sat)
