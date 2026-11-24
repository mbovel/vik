; Hand-written for VIK (this repository's license): an element read from an array of bit-vectors is in
; `[0, 2^n)` — the range is asserted at the `select`'s first use.
; The declarations are global, so that they stay over `reset-assertions`, which forgets the others (issue 246).
(set-option :global-declarations true)
(set-logic QF_ABV)
(declare-fun a () (Array (_ BitVec 32) (_ BitVec 8)))
(declare-fun i () (_ BitVec 32))
(assert (not (bvule (select a i) #xFF)))
(set-info :status unsat)
(check-sat)
(reset-assertions)
(assert (= (bvadd (select a i) #x01) #x00))
(assert (not (= (select (store a i (select a i)) i) #xFF)))
(set-info :status unsat)
(check-sat)
