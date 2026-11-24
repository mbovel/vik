; From smt-switch: qf_uf/test-quoted-symbols.smt2 (BSD-3-Clause, LICENSES/smt-switch.txt)
(set-logic QF_UF)
(declare-const |a b| Bool)
(declare-const a Bool)

(assert |a b|)
; |a| is the symbol a
(assert (not |a|))

(set-info :status sat)
(check-sat)

(assert a)

(set-info :status unsat)
(check-sat)
