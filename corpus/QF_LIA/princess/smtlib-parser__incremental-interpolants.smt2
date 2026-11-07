; From princess: smtlib-parser/incremental-interpolants.smt2 (BSD, LICENSES/princess.txt)
; logic: QF_LIA, the smallest the script fits, for AUFLIA (prune logic)
(set-logic QF_LIA)
(set-option :produce-interpolants true)

(declare-fun p () Bool)
(declare-fun x () Int)

(assert (=> p (> x 0)))
(assert p)

(set-info :status sat)
(check-sat)

(assert (< x 0))

; previously, this check-sat command led to an exception
(set-info :status unsat)
(check-sat)

; now get interpolants ...
