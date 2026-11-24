; From opensmt: base/generic/check-status-sat-ok.smt2 (MIT, LICENSES/opensmt.txt)
(set-logic QF_UF)
(set-info :status sat)

(assert true)
(set-info :status sat)
(check-sat)

