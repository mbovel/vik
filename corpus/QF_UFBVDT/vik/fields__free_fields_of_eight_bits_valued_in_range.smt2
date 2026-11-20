; Hand-written for VIK (this repository's license): the fields of `y` and `z` are terms no assertion names, of a
; sort with more values than the problem has terms. They get no term of their own, and the model gives each
; a value of the sort that no other term has: not 255, which `x` holds, and not 256 either, past the range,
; which a value counted past every known one was (issue 126, 2026-11-20).
; EXPECT: sat
; EXPECT: ((define-fun x () P (mk #b11111111)) (define-fun y () P (mk #b00000000)) (define-fun z () P (mk #b00000001)))
; EXPECT: (((ubv_to_int (f y)) 0) ((ubv_to_int (f z)) 1))
(set-logic ALL)
(set-option :produce-models true)
(declare-datatype P ((mk (f (_ BitVec 8)))))
(declare-const x P)
(declare-const y P)
(declare-const z P)
(assert (= (f x) #xFF))
(assert (distinct x y z))
(set-info :status sat)
(check-sat)
(get-model)
(get-value ((ubv_to_int (f y)) (ubv_to_int (f z))))
