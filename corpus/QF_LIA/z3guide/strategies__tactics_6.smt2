; From z3guide: 03 - strategies/03 - tactics.md (block 6) (MIT, LICENSES/z3guide.txt)
; statuses: the `unknown` ones answered by Z3 version 4.14.0 (statusstamp, 2026-09-03)
(declare-const x Int)
(declare-const y Int)
(declare-const z Int)

(assert (and (> x 0) (< x 10)))
(assert (and (> y 0) (< y 10)))
(assert (and (> z 0) (< z 10)))
(assert (= (+ (* 3 y) (* 2 x)) z))

(set-info :status sat)
(check-sat-using (then (using-params simplify :arith-lhs true :som true)
                       normalize-bounds
                       lia2pb
                       pb2bv
                       bit-blast
                       sat))
(get-model)

(reset)

(declare-const x Int)
(declare-const y Int)
(declare-const z Int)

(assert (= (+ (* 3 y) (* 2 x)) z))

;; The next command will fail since x, y and z are not bounded.
(check-sat-using (then (using-params simplify :arith-lhs true :som true)
                       normalize-bounds
                       lia2pb
                       pb2bv
                       bit-blast
                       sat))
(get-info :reason-unknown)
