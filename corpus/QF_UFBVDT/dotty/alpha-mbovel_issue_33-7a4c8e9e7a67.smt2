; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik), one implication checked by the qualifier solver while compiling the test `mbovel_issue_33.scala`, Int/Long as 32/64-bit vectors
; original path: tests/*-custom-args/qualified-types/mbovel_issue_33.scala, dumped with -Yqualified-types-smt-dump (bit-vector default)
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
(set-info :source |qualified types implication; e-graph: proved|)
(set-info :status unsat)
(set-logic QF_UFBVDT)
(declare-datatypes ((Foo 0)) (((new_Foo (Foo.a@Foo (_ BitVec 32)) (Foo.b@Foo (_ BitVec 32))))))
(declare-fun this_Foo () Foo)
(declare-fun x0 () (_ BitVec 32))
(assert (bvslt (_ bv10 32) (bvadd (Foo.a@Foo this_Foo) x0)))
(assert (not (bvslt (_ bv10 32) (bvadd (Foo.a@Foo this_Foo) x0))))
(check-sat)
