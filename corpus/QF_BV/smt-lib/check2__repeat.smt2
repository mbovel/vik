; From smt-lib: QF_BV/check2/repeat.smt2 (CC BY 4.0 (SMT-LIB release 2025, non-incremental, Zenodo 16740866))
(set-info :smt-lib-version 2.6)
(set-logic QF_BV)
(set-info :source |
	Constructed by Trevor Hansen to test a simple repeat
|)
(set-info :category "check")
(set-info :status unsat)
(declare-fun v0 () (_ BitVec 1))
(assert (not (= (concat v0 v0) ((_ repeat 2) v0))))
(check-sat)
(exit)
