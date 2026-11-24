; From yices2: iss433b.smt2 (BSD-3-Clause, LICENSES/yices2.txt)
(set-option :produce-models true)
(set-logic QF_UFBV)

(declare-sort State 0)
(declare-fun a (State) Bool)
(declare-fun b (State) Bool)
(declare-fun c (State) (_ BitVec 1))

(declare-const s0 State)
(declare-fun tmp () Bool)

(assert (bvult (c s0) #b1))
(assert (distinct (c s0) #b0))

(declare-const s1 State)
(define-fun s1-eq () Bool (= (a s1) (b s1)))

(assert (= tmp (= (a s1) (b s1))))
(set-info :status unsat)
(check-sat)
(set-info :status unsat)
(check-sat-assuming (tmp))
;(get-value ((c s0)))
