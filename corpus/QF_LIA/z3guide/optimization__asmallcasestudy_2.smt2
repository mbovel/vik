; From z3guide: 04 - optimization/06 - asmallcasestudy.md (block 2) (MIT, LICENSES/z3guide.txt)
; statuses: the `unknown` ones answered by Z3 version 4.14.0 (statusstamp, 2026-09-03)
; EXPECT: sat
; EXPECT: ((define-fun x11 () Int 0) (define-fun x12 () Int 0) (define-fun x13 () Int 1) (define-fun x21 () Int 0) (define-fun x22 () Int 0) (define-fun x23 () Int 1) (define-fun x31 () Int 0) (define-fun x32 () Int 0) (define-fun x33 () Int 1) (define-fun y1 () Int 0) (define-fun y2 () Int 0) (define-fun y3 () Int 1))
; logic: QF_LIA, the smallest the script fits, which set none (prune logic)
(set-logic QF_LIA)
(declare-const x11 Int)
(declare-const x12 Int)
(declare-const x13 Int)
(declare-const x21 Int)
(declare-const x22 Int)
(declare-const x23 Int)
(declare-const x31 Int)
(declare-const x32 Int)
(declare-const x33 Int)

(declare-const y1 Int)
(declare-const y2 Int)
(declare-const y3 Int)

(assert (and (>= x11 0) (>= x12 0) (>= x13 0) 
             (>= x21 0) (>= x22 0) (>= x23 0)
             (>= x31 0) (>= x32 0) (>= x33 0)))
             
(assert (and (<= y1 1) (<= y2 1) (<= y3 1)))

(assert (= (+ x11 x12 x13) 1))
(assert (= (+ x21 x22 x23) 1))
(assert (= (+ x31 x32 x33) 1))

(assert (and (>= y1 x11) (>= y1 x21) (>= y1 x31)))
(assert (and (>= y2 x12) (>= y2 x22) (>= y2 x32)))
(assert (and (>= y3 x13) (>= y3 x23) (>= y3 x33)))

(assert (<= (+ (* 100 x11) (* 50 x21) (* 15 x31)) (* 100 y1)))
(assert (<= (+ (* 100 x12) (* 50 x22) (* 15 x32)) (* 75 y2)))             
(assert (<= (+ (* 100 x13) (* 50 x23) (* 15 x33)) (* 200 y3)))

(minimize (+ y1 y2 y3))
(minimize (+ (* 10 y1) (* 5 y2) (* 20 y3)))

;(set-option :opt.priority pareto)
(set-info :status sat)
(check-sat)
(get-model)
(get-objectives)
