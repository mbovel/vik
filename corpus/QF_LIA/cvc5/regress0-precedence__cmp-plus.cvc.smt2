; From cvc5: regress0/precedence/cmp-plus.cvc.smt2 (modified BSD, LICENSES/cvc5.txt)
; EXPECT: unsat
(set-logic ALL)
(set-option :incremental false)
(declare-fun x () Int)
(declare-fun y () Int)
(declare-fun z () Int)
(set-info :status unsat)
(check-sat-assuming ( (not (let ((_let_1 (and (> (- (+ x y) z) 0) (< 0 (+ (- x y) z))))) (= _let_1 _let_1))) ))
