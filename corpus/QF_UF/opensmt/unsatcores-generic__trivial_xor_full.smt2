; From opensmt: unsatcores/generic/trivial_xor_full.smt2 (MIT, LICENSES/opensmt.txt)
(set-option :produce-unsat-cores true)
(set-option :print-cores-full true)

(set-logic QF_UF)

(declare-const b1 Bool)
(declare-const b2 Bool)

(assert (and b1 b2))
(assert (or b1 b2))
(assert (xor b1 b2))

(set-info :status unsat)
(check-sat)
(get-unsat-core)
