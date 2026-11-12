; From z3test: z3.4.smt2 (MIT, LICENSES/z3test.txt)
; logic: QF_UF, the smallest the script fits, which set none (prune logic)
(set-logic QF_UF)

; Copyright (c) 2015 Microsoft Corporation
(set-option :auto-config true)
(set-option :produce-models true)

(declare-const p Bool)
(declare-const q Bool)
(declare-const r Bool)
(define-fun conjecture () Bool
	(=> (and (=> p q) (=> q r))
		(=> p r)))
(assert (not conjecture))
(set-info :status unsat)
(check-sat)
