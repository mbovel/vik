; From smt-lib: QF_UF/20170829-Rodin/smt3809952321495040629.smt2 (CC BY 4.0 (SMT-LIB release 2025, non-incremental, Zenodo 16740866))
(set-info :smt-lib-version 2.6)
(set-logic QF_UF)
(set-info :source |Generator: Rodin SMT Plug-in|)
(set-info :license "https://creativecommons.org/licenses/by-nc/4.0/")
(set-info :category "industrial")
(set-info :status sat)

(declare-sort N 0)
(declare-fun M (N N) Bool)
(declare-fun bm (N) Bool)
(declare-fun n (N) Bool)
(declare-fun x () N)
(declare-fun y () N)

(assert (! (M x y)
         :named hyp1))
(assert (! (not 
               (bm y))
         :named hyp2))
(assert (! (not 
               (n x))
         :named goal))
(check-sat)
(exit)

