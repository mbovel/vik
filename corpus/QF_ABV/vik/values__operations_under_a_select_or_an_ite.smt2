; Hand-written for VIK (this repository's license): a bit-vector operation nested where the lowering
; mints a constant — under a `select`, under an `ite`, a division of an `ite`, an `extract` of one, a
; macro application, an implication or a `distinct` as the condition — valued as a literal of its sort
; (issue 48). VIK printed `q!2`, `r!19`, `bv!26`, an integer for a bit-vector, and 0 for a read at a
; minted index.
; EXPECT: sat
; EXPECT: (((select a (bvadd x #b0001)) #b0111) ((g x) #b0100) ((let ((y x)) (bvadd y #b0001)) #b0100) ((select arr x) #b0000) (arr ((as const (Array (_ BitVec 4) (_ BitVec 4))) #b0000)))
; EXPECT: (((bvudiv (ite (bvsle by #b0110) by by) #b0011) #b0010) ((bvlshr (ite (bvult by #b1000) by #b0111) #b0001) #b0011))
; EXPECT: ((((_ extract 2 1) (ite (bvult by #b1000) by #b0111)) #b11) ((bvadd (ite (bvult by #b1000) by #b0111) #b0001) #b1000))
; EXPECT: (((ite (=> p (bvule x #b0000)) (bvlshr #b0101 #b0001) by) #b0111) ((ite (xor p (bvule x #b0000)) by #b0001) #b0111) ((bvneg (ite (distinct x x #b0000) by #b0100)) #b1100))
; EXPECT: (((bvsdiv #b1000 #b0011) #b1110) ((bvsrem #b1000 #b0011) #b1110) ((bvsmod #b1000 #b0011) #b0001) ((bvsmod #b0111 #b1101) #b1110) ((bvudiv by #b0000) #b1111) ((bvurem by #b0000) #b0111) ((bvsdiv #b1000 #b0000) #b0001) ((bvashr #b1000 #b0110) #b1111) (((_ rotate_left 5) by) #b1110) (((_ sign_extend 2) #b1000) #b111000) ((concat x by) #b00110111))
(set-logic QF_ABV)
(set-option :produce-models true)
(declare-const x (_ BitVec 4))
(declare-const by (_ BitVec 4))
(declare-const p Bool)
(declare-const a (Array (_ BitVec 4) (_ BitVec 4)))
(declare-const arr (Array (_ BitVec 4) (_ BitVec 4)))
(define-fun g ((y (_ BitVec 4))) (_ BitVec 4) (bvadd y #b0001))
(assert (= x #b0011))
(assert (= by #b0111))
(assert p)
(assert (= (select a #b0100) #b0111))
(set-info :status sat)
(check-sat)
(get-value ((select a (bvadd x #b0001)) (g x) (let ((y x)) (bvadd y #b0001)) (select arr x) arr))
(get-value ((bvudiv (ite (bvsle by #b0110) by by) #b0011) (bvlshr (ite (bvult by #b1000) by #b0111) #b0001)))
(get-value (((_ extract 2 1) (ite (bvult by #b1000) by #b0111)) (bvadd (ite (bvult by #b1000) by #b0111) #b0001)))
(get-value ((ite (=> p (bvule x #b0000)) (bvlshr #b0101 #b0001) by) (ite (xor p (bvule x #b0000)) by #b0001) (bvneg (ite (distinct x x #b0000) by #b0100))))
(get-value ((bvsdiv #b1000 #b0011) (bvsrem #b1000 #b0011) (bvsmod #b1000 #b0011) (bvsmod #b0111 #b1101) (bvudiv by #b0000) (bvurem by #b0000) (bvsdiv #b1000 #b0000) (bvashr #b1000 #b0110) ((_ rotate_left 5) by) ((_ sign_extend 2) #b1000) (concat x by)))
