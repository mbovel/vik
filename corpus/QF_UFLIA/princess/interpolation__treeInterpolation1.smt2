; From princess: interpolation/treeInterpolation1.smt2 (BSD, LICENSES/princess.txt)
; logic: QF_UFLIA, the smallest the script fits, which set none (prune logic)
(set-logic QF_UFLIA)
(declare-fun f (Int) Int)
(declare-const x Int)
(declare-const y Int)

(set-option :produce-interpolants true)

(assert (! (>= (f x) 0) :named A))
(assert (! (< (f y) 0) :named B))
(assert (! (= x y) :named C))

(set-info :status unsat)
(check-sat)
(get-interpolants A (B) C)
