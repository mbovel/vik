; Hand-written for VIK (this repository's license): a quoted symbol is the text between its bars, and
; the same symbol as that text unquoted — `|p1|` and `p1` are one (SMT-LIB 2.6, 3.1). The parser kept the
; bars, so a file that declares `p1` and asserts about `|p1|` was rejected as an unknown symbol; the
; SMT-LIB release has 46 such files in `20210312-Bouvier` (2026-09-04). A quoted symbol whose text is a
; numeral or a keyword is not that token, and keeps its bars.
(set-logic QF_UFLIA)
(declare-const p1 Int)
(declare-fun |286| (Int) Bool)
(declare-const |a b| Int)
(assert (= |p1| 3))
(assert (> p1 |a b|))
(assert (|286| p1))
(set-info :status sat)
(check-sat)
(assert (not (|286| |p1|)))
(set-info :status unsat)
(check-sat)
(reset-assertions)
(assert (= p1 (+ |a b| 1)))
(assert (distinct |p1| (+ |a b| 1)))
(set-info :status unsat)
(check-sat)
