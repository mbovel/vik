; From z3test: strategy-par-or-3.smt2 (MIT, LICENSES/z3test.txt)
;; Copyright (c) 2016 Microsoft Corporation 
; logic: QF_UF, the smallest the script fits, which set none (prune logic)
(set-logic QF_UF)

(set-info :source |Written by D. B. Staple for GitHub issue #629.|)
(set-info :status sat)

(declare-fun a0 () Bool)
(declare-fun a1 () Bool)
(declare-fun c0 () Bool)
(declare-fun c1 () Bool)
(declare-fun b0 () Bool)
(declare-fun b1 () Bool)
(declare-fun b2 () Bool)
(assert (or
    (not (=> (and (and (not b2) (not b1)) (not b0)) (= c0 a0)))
    (not (=> (and (and (not b2) (not b1)) (not b0)) (= c1 a1)))))
(set-info :status sat)
(check-sat-using (and-then (par-or qfbv qfbv)))
