; From stp: smt2-command-tests/global-declarations-true-assertions-not-global.smt2 (MIT, LICENSES/stp.txt)
; The option makes declarations global, not assertions: pop still undoes every
; assertion made in the level it removes (SMT-LIB 2.6, section 4.1.4). The two
; conflicting assertions below go away with the pop, while x stays declared, so
; the second query is satisfiable.
; RUN: %solver %s | %OutputCheck %s
(set-option :global-declarations true)
(set-logic QF_BV)
(push 1)
(declare-fun x () (_ BitVec 4))
(assert (= x #x1))
(assert (= x #x2))
; CHECK: ^unsat$
(set-info :status unsat)
(check-sat)
(pop 1)
(assert (= x #x1))
; CHECK-NEXT: ^sat$
(set-info :status sat)
(check-sat)
