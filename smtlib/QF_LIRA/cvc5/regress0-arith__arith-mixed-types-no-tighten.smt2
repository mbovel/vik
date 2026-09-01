; From cvc5: regress0/arith/arith-mixed-types-no-tighten.smt2 (modified BSD, LICENSES/cvc5.txt)
; COMMAND-LINE:
; EXPECT: unsat
(set-logic QF_LIRA)

(declare-fun x () Real)
(declare-fun n () Int)

; Even though `n` is an integer, this would be UNSAT for real `n`, so the integrality can be ignored.
(assert
    (and
        (>= (+ x (to_real n)) 1.0)
        (<= n 0)
        (<= x 0.0)
    )
)

(set-info :status unsat)
(check-sat)

