; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik), one implication checked by the qualifier solver while compiling the test `adapt_neg.scala`, Int/Long as 32/64-bit vectors
; original path: tests/*-custom-args/qualified-types/adapt_neg.scala, dumped with -Yqualified-types-smt-dump (bit-vector default)
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; untyped: <noprefix>
; statuses: the `unknown` ones answered by Z3 version 4.14.0 (statusstamp, 2026-09-03)
(set-info :source |qualified types implication; e-graph: not proved|)
(set-info :status sat)
(set-logic QF_UFBVDT)
(declare-datatypes ((Box_scala.Int 0)) (((new_Box_scala.Int (Box.x@Box_scala.Int (_ BitVec 32))))))
(declare-fun x0 () Box_scala.Int)
(declare-fun x () (_ BitVec 32))
(assert true)
(assert (not (= x0 (new_Box_scala.Int (bvadd (_ bv1 32) x)))))
(check-sat)
