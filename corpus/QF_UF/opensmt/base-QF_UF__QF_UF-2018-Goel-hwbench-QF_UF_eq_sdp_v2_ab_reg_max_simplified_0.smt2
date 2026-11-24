; From opensmt: base/QF_UF/QF_UF-2018-Goel-hwbench-QF_UF_eq_sdp_v2_ab_reg_max_simplified_0.smt2 (MIT, LICENSES/opensmt.txt)
(set-logic QF_UF)
(declare-fun Add (Bool Bool) Bool)
(declare-fun b () Bool)

(assert (not (Add false b)))
(set-info :status sat)
(check-sat)
(exit)
