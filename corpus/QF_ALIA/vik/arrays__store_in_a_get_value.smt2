; Hand-written for VIK (this repository's license): a `store` first written in a `get-value`, then in an
; assertion. The query minted the store's symbol and threw its declaration away with the query, while the
; cache kept the symbol, so the later assertion reused a symbol the solver never knew and got no
; read-over-write axiom: a contradiction answered sat (issue 16, 2026-09-05).
; EXPECT: sat
; EXPECT: (((select (store a 1 v) 1) 7))
; EXPECT: unsat
(set-logic QF_ALIA)
(set-option :produce-models true)
(declare-fun a () (Array Int Int))
(declare-fun v () Int)
(assert (= v 7))
(set-info :status sat)
(check-sat)
(get-value ((select (store a 1 v) 1)))
(assert (not (= (select (store a 1 v) 1) v)))
(set-info :status unsat)
(check-sat)
