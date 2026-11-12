; From cvc5: regress0/push-pop/simple_unsat_cores.smt2 (modified BSD, LICENSES/cvc5.txt)
; COMMAND-LINE: --incremental
; EXPECT: unsat
; EXPECT: unsat
; logic: QF_UF, the smallest the script fits, for UF (prune logic)
(set-logic QF_UF)
(push 1)
(assert false)
(set-info :status unsat)
(check-sat)
(pop 1)
(assert false)
(set-info :status unsat)
(check-sat)
