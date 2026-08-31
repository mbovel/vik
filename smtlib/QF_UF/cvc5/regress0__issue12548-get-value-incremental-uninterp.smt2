; From cvc5: regress0/issue12548-get-value-incremental-uninterp.smt2 (modified BSD, LICENSES/cvc5.txt)
; COMMAND-LINE: --incremental
; EXIT: 0
; EXPECT: unsat
; EXPECT: (error "cannot get value unless after a SAT or UNKNOWN response.")
; EXPECT: sat
; EXPECT: ((x (as @U_0 U)))
(set-option :produce-models true)
(set-logic QF_UF)
(declare-sort U 0)
(declare-const x U)
(set-info :status unsat)
(check-sat-assuming (false))
(get-value (x))
(set-info :status sat)
(check-sat-assuming (true))
(get-value (x))
