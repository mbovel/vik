; From princess: smtlib-parser/define-fun-interpolation.smt2 (BSD, LICENSES/princess.txt)
; logic: QF_LIA, the smallest the script fits, for AUFLIA (prune logic)

(set-logic QF_LIA)

(set-option :produce-interpolants true)

(declare-fun c () Int)
(declare-fun d () Int)
(define-fun f ((x Int) (y Int)) Int (+ x y))

(assert (> (f c d) 0))
(assert (< (f d c) 0))

(set-info :status unsat)
(check-sat)
(get-interpolants)
