; From smt-lib: QF_LIA/cut_lemmas/10-vars/cut_lemma_01_001.smt2 (CC BY 4.0 (SMT-LIB release 2025, non-incremental, Zenodo 16740866))
(set-info :smt-lib-version 2.6)
(set-logic QF_LIA)
(set-info :source |
Alberto Griggio

|)
(set-info :category "crafted")
(set-info :status unsat)
(declare-fun x3 () Int)
(declare-fun x4 () Int)
(declare-fun x7 () Int)
(declare-fun x8 () Int)
(assert (and (not (>= (+ (* x4 2) (+ (* x7 (- 2)) (* x8 (- 2)))) 2)) (and (and (not (<= 1 x4)) (<= (+ (+ (+ (- x3) x8) (* x4 (- 2))) x7) (- 1))) (<= (+ (+ x3 x8) x7) 0))))
(check-sat)
(exit)
