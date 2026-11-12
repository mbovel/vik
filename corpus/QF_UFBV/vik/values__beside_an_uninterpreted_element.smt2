; Hand-written for VIK (this repository's license): bit-vector items beside one of an uninterpreted sort,
; and the model of a bit-vector constant. One item the lowering could not compute sent every item down the
; fallback path, where a bit-vector read as 0; and the model printed the lowering's `Int 10` for a
; `(_ BitVec 4)` constant (issue 28, 2026-09-05).
; EXPECT: sat
; EXPECT: ((a @U!0) (x #b1010) ((bvor x y) #b1110) ((bvadd x y) #b0000))
; EXPECT: ((define-fun a () U @U!0) (define-fun x () (_ BitVec 4) #b1010) (define-fun y () (_ BitVec 4) #b0110))
; logic: QF_UFBV, the smallest the script fits, for ALL (prune logic)
(set-logic QF_UFBV)
(set-option :produce-models true)
(declare-sort U 0)
(declare-const a U)
(declare-const x (_ BitVec 4))
(declare-const y (_ BitVec 4))
(assert (= x #b1010))
(assert (= y #b0110))
(set-info :status sat)
(check-sat)
(get-value (a x (bvor x y) (bvadd x y)))
(get-model)
