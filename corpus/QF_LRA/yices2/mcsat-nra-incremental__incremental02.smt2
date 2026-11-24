; From yices2: mcsat/nra/incremental/incremental02.smt2 (BSD-3-Clause, LICENSES/yices2.txt)
; logic: QF_LRA, the smallest the script fits, for QF_NRA (prune logic)
(set-logic QF_LRA)
(set-info :smt-lib-version 2.0)
(declare-fun x () Real)

;; SAT
(push 1)
(assert (= x 0))
(set-info :status sat)
(check-sat)
(get-model)

;; UNSAT
(push 1)
(assert (or (< x 0) (> x 0)))
(set-info :status unsat)
(check-sat)

;; SAT
(pop 1)
(set-info :status sat)
(check-sat)
(get-model)

;; SAT
(pop 1)
(set-info :status sat)
(check-sat)
(get-model)

(exit)
