; Hand-written for VIK (this repository's license): `get-value` of bit-vector operations. The value of a
; term the lowering gives fresh constants was the constant's name, since the model knows the declared
; symbols and not the lowering's; it is computed from the declared symbols instead (2026-09-04).
; EXPECT: sat
; EXPECT: ((x #b00000011) ((bvadd x y) #b00000111) ((bvmul x y) #b00001100) ((bvand x y) #b00000000) ((bvor x y) #b00000111) ((bvxor x y) #b00000111) ((bvudiv y x) #b00000001) ((bvurem y x) #b00000001))
; EXPECT: (((bvshl x y) #b00110000) ((bvlshr y x) #b00000000) ((bvashr x y) #b00000000) ((bvneg x) #b11111101) ((bvnot x) #b11111100) ((concat x y) #b0000001100000100))
; EXPECT: ((((_ extract 3 0) (bvadd x y)) #b0111) (((_ zero_extend 4) x) #b000000000011) (((_ sign_extend 4) x) #b000000000011) (((_ rotate_left 3) x) #b00011000))
; EXPECT: (((bvult x y) true) ((bvsle y x) false) ((= x y) false) ((distinct x y) true) ((bvcomp x y) #b0))
(set-logic QF_BV)
(declare-const x (_ BitVec 8))
(declare-const y (_ BitVec 8))
(assert (= x #x03))
(assert (= y #x04))
(set-info :status sat)
(check-sat)
(get-value (x (bvadd x y) (bvmul x y) (bvand x y) (bvor x y) (bvxor x y) (bvudiv y x) (bvurem y x)))
(get-value ((bvshl x y) (bvlshr y x) (bvashr x y) (bvneg x) (bvnot x) (concat x y)))
(get-value (((_ extract 3 0) (bvadd x y)) ((_ zero_extend 4) x) ((_ sign_extend 4) x) ((_ rotate_left 3) x)))
(get-value ((bvult x y) (bvsle y x) (= x y) (distinct x y) (bvcomp x y)))
