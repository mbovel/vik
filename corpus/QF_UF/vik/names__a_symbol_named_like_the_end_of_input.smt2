; Hand-written for VIK (this repository's license): a constant named `$EOF`, the reader's own word for the
; end of its input, which it told apart by value: a script holding the symbol was cut short there without
; a word (issue 29, 2026-09-05).
(set-logic QF_UF)
(declare-fun $EOF () Bool)
(declare-fun p () Bool)
(assert $EOF)
(assert (=> $EOF p))
(set-info :status sat)
(check-sat)
(assert (not p))
(set-info :status unsat)
(check-sat)
