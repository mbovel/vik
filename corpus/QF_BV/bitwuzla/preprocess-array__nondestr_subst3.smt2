; From bitwuzla: preprocess/array/nondestr_subst3.smt2 (MIT, LICENSES/bitwuzla.txt)
; logic: QF_BV, the smallest the script fits, for QF_ABV (prune logic)
(set-logic QF_BV)
(set-info :status sat)
(declare-fun a () (_ BitVec 2))
(declare-fun b () (_ BitVec 2))
(declare-fun c () Bool)
(declare-fun d () Bool)

(define-fun |UNROLL#7933| () Bool (= a b))
(define-fun |UNROLL#8491| () Bool (and (= (= b (_ bv0 2)) d) c))

(assert (or (= b (_ bv0 2)) (not c )))

(assert c)

(check-sat)
(exit)
