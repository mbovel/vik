; Hand-written for VIK (this repository's license): the `match` rewrite binds the scrutinee to a name of
; its own, `match!N`, which must stay clear of the names the script binds around the match — a `let`'s
; and a macro's parameters — since a case body may read them (issue 42). VIK skipped the declared
; symbols only and read the scrutinee for `match!2` and `match!1`, answering sat on both checks.
(set-logic ALL)
(declare-datatype L ((nil) (cons (h Int) (t L))))
(define-fun g ((match!1 L) (x L)) L (match x ((nil nil) ((cons a b) match!1))))
(declare-const x L)
(declare-const y L)
(assert (= x (cons 1 nil)))
(assert (= y nil))
(push 1)
(assert (= (let ((match!1 y) (match!2 y) (match!3 y) (match!4 y)) (match x ((nil nil) ((cons a b) match!2)))) x))
(set-info :status unsat)
(check-sat)
(pop 1)
(push 1)
(assert (= (g y x) x))
(set-info :status unsat)
(check-sat)
(pop 1)
(assert (= (g x x) x))
(set-info :status sat)
(check-sat)
