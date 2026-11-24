; From stp: smt2-command-tests/global-declarations-true-pop-definition.smt2 (MIT, LICENSES/stp.txt)
; Definitions are global under the option just as declarations are, and a
; definition made global keeps meaning what it meant: x!2 is still (not x!1)
; after the pop, so asserting both is unsatisfiable rather than satisfiable.
; RUN: %solver %s | %OutputCheck %s
(set-option :global-declarations true)
(set-logic QF_BV)
(push 1)
(declare-fun x!1 () Bool)
(define-fun x!2 () Bool (not x!1))
(pop 1)
(assert (not x!1))
(assert (not x!2))
; CHECK: ^unsat$
(set-info :status unsat)
(check-sat)
