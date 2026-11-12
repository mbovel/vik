; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik, commit e7c4e412ef), one implication checked by the qualifier solver while compiling the test `range.scala`
; original path: tests/*-custom-args/qualified-types/range.scala, dumped with -Yqualified-types-smt-dump
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; untyped: Any
; logic: QF_UFLIA, narrowed from QF_UFDTLIA, which claims more than the script uses (prune logic)
(set-info :source |qualified types implication; e-graph: proved|)
(set-info :status unsat)
(set-logic QF_UFLIA)
(declare-sort scala.Product 0)
(declare-fun x$0 () scala.Product)
(declare-fun Product.productElement@scala.Product (scala.Product Int) Int)
(declare-fun from$1 () Int)
(declare-fun x0 () Int)
(assert (= (Product.productElement@scala.Product x$0 0) from$1))
(assert (= (Product.productElement@scala.Product x$0 0) from$1))
(assert (< from$1 x0))
(assert (not (< from$1 x0)))
(check-sat)
