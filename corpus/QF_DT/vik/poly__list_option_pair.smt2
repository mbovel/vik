; Hand-written for VIK (this repository's license): parametric datatypes,
; instantiated sort by sort — `(List Int)` and `(List Bool)` are two
; datatypes, with their own constructors — nested instances, a pair of two
; parameters, `as` on the ambiguous `nil`, and `match` over an instance.
; EXPECT: sat
; EXPECT: (((first p) 2) ((second p) true) ((match l ((nil 0) ((cons h _) h))) 1) ((match o ((none 0) ((some x) (head x)))) 1))
; EXPECT: unsat
; EXPECT: unsat
(set-logic ALL)
(set-option :produce-models true)
(declare-datatype List (par (T) ((nil) (cons (head T) (tail (List T))))))
(declare-datatype Option (par (X) ((none) (some (val X)))))
(declare-datatypes ((Pair 2)) ((par (A B) ((pair (first A) (second B))))))
(declare-const l (List Int))
(declare-const m (List Bool))
(declare-const o (Option (List Int)))
(declare-const p (Pair Int Bool))
(assert (= l (cons 1 (cons 2 (as nil (List Int))))))
(assert (= m (cons true (as nil (List Bool)))))
(assert (= o (some l)))
(assert (= p (pair (head (tail l)) (head m))))
(assert (= (head (val o)) 1))
(set-info :status sat)
(check-sat)
(get-value ((first p) (second p) (match l ((nil 0) ((cons h _) h))) (match o ((none 0) ((some x) (head x))))))
(push 1)
(assert ((_ is nil) (tail (tail l))))
(assert (not (= (first p) 2)))
(set-info :status unsat)
(check-sat)
(pop 1)
(push 1)
(assert (= m (as nil (List Bool))))
(set-info :status unsat)
(check-sat)
(pop 1)
