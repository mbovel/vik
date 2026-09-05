; Hand-written for VIK (this repository's license): a constant named `match!1`, the name the match
; compiler gave its first scrutinee. The compiler's `let` shadowed the constant inside the match, so the
; branch that returned it returned the scrutinee instead, and a contradiction answered sat (issue 21,
; 2026-09-05). The compiler now passes over the script's own names.
(set-logic ALL)
(declare-datatype Bx ((mk (val Int)) (none)))
(declare-const b Bx)
(declare-const c Bx)
(declare-const |match!1| Bx)
(assert ((_ is none) b))
(assert ((_ is mk) |match!1|))
(assert (= c (match b ((none |match!1|) ((mk v) b)))))
(set-info :status sat)
(check-sat)
(assert ((_ is none) c))
(set-info :status unsat)
(check-sat)
