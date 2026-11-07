; From z3guide: 03 - strategies/03 - tactics.md (block 7) (MIT, LICENSES/z3guide.txt)
; statuses: the `unknown` ones answered by Z3 version 4.14.0 (statusstamp, 2026-09-03)
; EXPECT: sat
; EXPECT: ((define-fun x () (_ BitVec 64) #b0000000000000000000000000000000000000000000000000000000000001100) (define-fun y () (_ BitVec 64) #b0000000000000000000000000000000000000000000000000000000000001100) (define-fun z () (_ BitVec 64) #b1000000000000000000000000000000000000000000000000000000000000110))
; EXPECT: sat
; EXPECT: ((define-fun x () (_ BitVec 64) #b0000000000000000000000000000000000000000000000000000000000001100) (define-fun y () (_ BitVec 64) #b0000000000000000000000000000000000000000000000000000000000001100) (define-fun z () (_ BitVec 64) #b1000000000000000000000000000000000000000000000000000000000000110))
; logic: QF_BV, the smallest the script fits, which set none (prune logic)
(set-logic QF_BV)
(declare-const x (_ BitVec 64))
(declare-const y (_ BitVec 64))
(declare-const z (_ BitVec 64))

(assert (bvsgt x (_ bv10 64)))
(assert (= x y))
(assert (= y (bvadd z z)))

(set-info :status sat)
(check-sat-using 
 (par-or
  ;; Strategy 1: use bit-blasting
  (then simplify bit-blast sat)
  ;; Strategy 2: use default solver
  smt))

(get-model)

(echo "using different random seeds...")

(set-info :status sat)
(check-sat-using
 (par-or
  ;; Strategy 1: using seed 1
  (using-params smt :random-seed 1)
  ;; Strategy 1: using seed 2
  (using-params smt :random-seed 2)
  ;; Strategy 1: using seed 3
  (using-params smt :random-seed 3)))

(get-model)
