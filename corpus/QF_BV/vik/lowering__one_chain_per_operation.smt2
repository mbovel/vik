; Hand-written for VIK (this repository's license): a term and its own negation, from the Scala 3
; qualified-types checker (`subtyping_normalization-aefd11751e2a`). The lowering gave the second
; `bvmul 4 (bvadd 1 x)` its own chain of fresh constants, so the solver had to prove the two chains equal
; by an integer argument over 2^32, and branch and bound ran for minutes; one chain per distinct operation
; at a level makes it syntactic (2026-09-03).
(set-logic QF_BV)
(declare-fun x0 () (_ BitVec 32))
(declare-fun x () (_ BitVec 32))
(assert (= x0 (bvmul (_ bv4 32) (bvadd (_ bv1 32) x))))
(assert (not (= x0 (bvmul (_ bv4 32) (bvadd (_ bv1 32) x)))))
(set-info :status unsat)
(check-sat)
