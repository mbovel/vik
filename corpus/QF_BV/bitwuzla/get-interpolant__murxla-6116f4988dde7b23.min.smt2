; From bitwuzla: get-interpolant/murxla-6116f4988dde7b23.min.smt2 (MIT, LICENSES/bitwuzla.txt)
; ignore output
; logic: QF_BV, the smallest the script fits, which set none (prune logic)
(set-logic QF_BV)
(declare-const x (_ BitVec 22))
(set-option :produce-interpolants 1)
(assert (bvsgt (_ bv0 38) ((_ sign_extend 16) x)))
(set-info :status sat)
(check-sat)
