; Hand-written for VIK (this repository's license): a bit-vector inside a datatype or an array is written
; as one in a value and in the model — `#xa` inside `(mk #xa 7)`, an array of `(_ BitVec 8)` over a
; constant array of that sort (issue 47). VIK wrote the lowered integers, `(mk 10 7)` and an array of
; `(Array (_ BitVec 8) Int)`, which z3 rejects as a model.
; EXPECT: sat
; EXPECT: ((p (mk #b1010 7)) ((fst p) #b1010) ((f #x1) #b1001) ((select a #x01) #b00000101) ((mk (bvadd (fst p) #x1) 2) (mk #b1011 2)) ((select a (bvadd #x00 #x01)) #b00000101))
; EXPECT: ((define-fun a () (Array (_ BitVec 8) (_ BitVec 8)) (store ((as const (Array (_ BitVec 8) (_ BitVec 8))) #b00000000) #b00000001 #b00000101)) (define-fun p () P (mk #b1010 7)) (define-fun f ((x!0 (_ BitVec 4))) (_ BitVec 4) (ite (= x!0 #b0001) #b1001 #b1001)))
(set-logic ALL)
(set-option :produce-models true)
(declare-datatype P ((mk (fst (_ BitVec 4)) (snd Int))))
(declare-const p P)
(declare-fun f ((_ BitVec 4)) (_ BitVec 4))
(declare-const a (Array (_ BitVec 8) (_ BitVec 8)))
(assert (= p (mk #xA 7)))
(assert (= (f #x1) #x9))
(assert (= (select a #x01) #x05))
(set-info :status sat)
(check-sat)
(get-value (p (fst p) (f #x1) (select a #x01) (mk (bvadd (fst p) #x1) 2) (select a (bvadd #x00 #x01))))
(get-model)
