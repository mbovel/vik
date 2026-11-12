; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik, commit e7c4e412ef), one implication checked by the qualifier solver while compiling the test `fact_unfold_neg.scala`
; original path: tests/*-custom-args/qualified-types/fact_unfold_neg.scala, dumped with -Yqualified-types-smt-dump
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; statuses: the `unknown` ones answered by Z3 version 4.14.0 (statusstamp, 2026-09-03)
; logic: QF_UFLIA, narrowed from QF_UFDTLIA, which claims more than the script uses (prune logic)
(set-info :source |qualified types implication; e-graph: not proved|)
(set-info :status sat)
(set-logic QF_UFLIA)
(declare-fun n () Int)
(declare-fun fact_unfold_neg$package$.fact (Int) Int)
(declare-fun mul (Int Int) Int)
(declare-fun x0 () Int)
(assert (= (fact_unfold_neg$package$.fact (+ (- 1) n)) (ite (= 0 (+ (- 1) n)) 1 (mul (+ (- 1) n) (fact_unfold_neg$package$.fact (+ (- 2) n))))))
(assert (= (fact_unfold_neg$package$.fact (+ (- 1) n)) (ite (= 0 (+ (- 1) n)) 1 (mul (+ (- 1) n) (fact_unfold_neg$package$.fact (+ (- 2) n))))))
(assert (= (mul n (fact_unfold_neg$package$.fact (+ (- 1) n))) x0))
(assert (not (= x0 (ite (= 0 n) 1 (mul n (fact_unfold_neg$package$.fact (+ (- 1) n)))))))
(check-sat)
