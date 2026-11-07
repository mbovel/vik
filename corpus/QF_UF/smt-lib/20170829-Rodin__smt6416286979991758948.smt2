; From smt-lib: QF_AUFLIA/20170829-Rodin/smt6416286979991758948.smt2 (CC BY 4.0 (SMT-LIB release 2025, non-incremental, Zenodo 16740866))
; logic: QF_UF, the smallest the script fits, for QF_AUFLIA (prune logic)
(set-info :smt-lib-version 2.6)
(set-logic QF_UF)
(set-info :source |Generator: Rodin SMT Plug-in|)
(set-info :license "https://creativecommons.org/licenses/by-nc/4.0/")
(set-info :category "industrial")
(set-info :status sat)


(assert (! (not 
               false)
         :named goal))
(check-sat)
(exit)

