; From smt-lib: QF_AUFLIA/20170829-Rodin/smt3150137541310906277.smt2 (CC BY 4.0 (SMT-LIB release 2025, non-incremental, Zenodo 16740866))
(set-info :smt-lib-version 2.6)
(set-logic QF_AUFLIA)
(set-info :source |Generator: Rodin SMT Plug-in|)
(set-info :license "https://creativecommons.org/licenses/by-nc/4.0/")
(set-info :category "industrial")
(set-info :status unsat)


(assert (! (not 
               (<= 1 1))
         :named goal))
(check-sat)
(exit)

