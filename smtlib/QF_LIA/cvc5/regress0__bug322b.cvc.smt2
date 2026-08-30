; From cvc5: regress0/bug322b.cvc.smt2 (modified BSD, LICENSES/cvc5.txt)
; COMMAND-LINE: --incremental
; EXPECT: unsat
; EXPECT: unsat
; EXPECT: unsat
(set-logic ALL)
(declare-fun x () Int)
(define-fun y () Int (+ x 1))
(define-fun z () Int (- 10))
(define-fun identity ((x Int)) Int x)
(set-info :status unsat)
(check-sat-assuming ( (not (= (identity x) x)) ))
(set-info :status unsat)
(check-sat-assuming ( (not (> (identity (+ x 1)) x)) ))
(set-info :status unsat)
(check-sat-assuming ( (not (let ((_let_1 (- 10))) (= (identity _let_1) _let_1))) ))
