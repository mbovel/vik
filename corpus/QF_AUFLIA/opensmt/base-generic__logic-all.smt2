; From opensmt: base/generic/logic-all.smt2 (MIT, LICENSES/opensmt.txt)
; logic: QF_AUFLIA, the smallest the script fits, for ALL (prune logic)
(set-logic QF_AUFLIA)
(declare-fun a () (Array Int Int))
(assert (= 1 (select a 0)))
(set-info :status sat)
(check-sat)

