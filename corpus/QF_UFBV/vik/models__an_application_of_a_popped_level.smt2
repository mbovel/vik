; Hand-written for VIK (this repository's license): an application whose argument a popped level minted. The
; encoder keeps the terms of a popped level, and the model gave `f` a row for `(f (bvadd w x))`, whose sum is
; a constant of that level with no range any more: its value was -1, which no bit-vector of 8 bits is, and
; the model check refused the next check's model, which answered unknown (issue 244, from STP's
; `uf/incremental-sigma0-restore`). A row for a term that mentions a popped symbol is left out.
(set-logic QF_UFBV)
(declare-fun x () (_ BitVec 8))
(declare-fun w () (_ BitVec 8))
(declare-fun y () (_ BitVec 8))
(declare-fun f ((_ BitVec 8)) (_ BitVec 8))
(push 1)
(assert false)
(assert (= y (f (bvadd w x))))
(set-info :status unsat)
(check-sat)
(pop 1)
(set-info :status sat)
(check-sat)
(assert (= (f (bvadd w x)) #x03))
(assert (= (f w) #x04))
(assert (= x #x00))
(set-info :status unsat)
(check-sat)
