; Hand-written for VIK (this repository's license): unsat cores under every state the session keeps —
; after a `reset-assertions`, under two levels, after a `pop`, with a named assertion of a popped level gone.
; Each core is judged by z3 (bench `CoreCheck`): the script with the named assertions outside the core
; dropped must be unsat. The second core is not minimal, which the standard allows.
; EXPECT: unsat
; EXPECT: (big small)
; EXPECT: unsat
; EXPECT: (fx fy xy)
; EXPECT: unsat
; EXPECT: (p1 p2)
; EXPECT: sat
; EXPECT: unsat
; EXPECT: (p1 p3 p4)
; EXPECT: unsat
; EXPECT: (p5 p6)
(set-logic QF_UFLIA)
(set-option :produce-unsat-cores true)
(set-option :global-declarations true)
(declare-const x Int) (declare-const y Int) (declare-fun f (Int) Int)
(assert (! (> x 5) :named big)) (assert (! (< y 3) :named small)) (assert (< x y))
(set-info :status unsat)
(check-sat)
(get-unsat-core)
(reset-assertions)
(assert (! (= (f x) 1) :named fx)) (assert (! (= (f y) 2) :named fy)) (assert (! (= x y) :named xy)) (assert (! (> x 0) :named pos))
(set-info :status unsat)
(check-sat)
(get-unsat-core)
(reset-assertions)
(push 1)
(assert (! (> x 10) :named p1))
(push 1)
(assert (! (< x 5) :named p2))
(set-info :status unsat)
(check-sat)
(get-unsat-core)
(pop 1)
(assert (! (< x 20) :named p3))
(set-info :status sat)
(check-sat)
(assert (! (> x 30) :named p4))
(set-info :status unsat)
(check-sat)
(get-unsat-core)
(pop 1)
(assert (! (> x 30) :named p5)) (assert (! (< x 20) :named p6))
(set-info :status unsat)
(check-sat)
(get-unsat-core)
