; Hand-written for VIK (this repository's license): stores at every value of a finite index sort make a
; constant array. With no declared array in the script, the model compared two array values as terms, and the
; check refused `((as const (Array Bool Int)) 0)` = `(store (store ((as const (Array Bool Int)) 1) true 0) false 0)`:
; `unknown` under `:check-models`. A declared array equal to such a chain was printed as eight stores (issue 255).
; EXPECT: sat
; EXPECT: sat
; EXPECT: ((i #b0))
; EXPECT: sat
; EXPECT: ((b ((as const (Array Bool (_ BitVec 4))) #b0000)) ((select b true) #b0000))
(set-logic ALL)
(set-option :produce-models true)
(declare-const i (_ BitVec 1))
(declare-const b (Array Bool (_ BitVec 4)))
(push 1)
(assert (= ((as const (Array Bool (_ BitVec 4))) #x0) (store (store ((as const (Array Bool (_ BitVec 4))) #x1) true #x0) false #x0)))
(set-info :status sat)
(check-sat)
(pop 1)
(push 1)
(assert (= ((as const (Array (_ BitVec 1) (_ BitVec 1))) #b0) (store (store ((as const (Array (_ BitVec 1) (_ BitVec 1))) #b1) i #b0) #b1 #b0)))
(set-info :status sat)
(check-sat)
(get-value (i))
(pop 1)
(assert (= b (store (store ((as const (Array Bool (_ BitVec 4))) #x1) true #x0) false #x0)))
(assert (= b ((as const (Array Bool (_ BitVec 4))) #x0)))
(set-info :status sat)
(check-sat)
(get-value (b (select b true)))
