; From cvc5: regress0/arith/integers/arith-int-042.min.cvc.smt2 (modified BSD, LICENSES/cvc5.txt)
; EXPECT: unsat
(set-logic ALL)
(set-option :incremental false)
(declare-fun x1 () Int)
(declare-fun x0 () Int)
(set-info :status unsat)
(check-sat-assuming ( (= (+ (* x0 6) (* x1 32)) 1) ))
