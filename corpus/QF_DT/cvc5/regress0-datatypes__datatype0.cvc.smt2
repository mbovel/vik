; From cvc5: regress0/datatypes/datatype0.cvc.smt2 (modified BSD, LICENSES/cvc5.txt)
; EXPECT: unsat
; logic: QF_DT, the smallest the script fits, for ALL (prune logic)
(set-logic QF_DT)
(set-option :incremental false)
(declare-datatypes ((nat 0)) (((succ (pred nat)) (zero))))
(declare-fun x () nat)
(set-info :status unsat)
(check-sat-assuming ( (not (or ((_ is succ) x) ((_ is zero) x))) ))
