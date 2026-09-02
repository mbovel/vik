package net.bovel.vik

import scala.collection.immutable.ArraySeq

import net.bovel.smtlib.{Parser, Printer}
import Term.{App, Const, Num}

/** The examples of the README's Usage section, as written there. */
class ReadmeExamplesTest extends munit.FunSuite:
  val (x, y) = (Const("x"), Const("y"))

  test("smt: uninterpreted functions"):
    val solver = Solver()
    val (a, b, f) = (Const("a"), Const("b"), Const("f"))
    solver.assert(a === b)
    solver.assert(App(f, List(a)) !== App(f, List(b)))
    assertEquals(solver.solve(), solver.Result.Unsat)

  test("smt: linear integer arithmetic"):
    val lia = Solver()
    lia.assert(x + y <= Num(1))
    lia.assert(x >= Num(1))
    lia.assert(y >= Num(1))
    assertEquals(lia.solve(), lia.Result.Unsat)

  test("smt: push and pop"):
    val s = Solver()
    s.assert(x + y <= Num(3))
    s.assert(x >= Num(1))
    assertEquals(s.solve(), s.Result.Sat)
    s.push()
    s.assert(y >= Num(3))
    assertEquals(s.solve(), s.Result.Unsat)
    s.pop()
    assertEquals(s.solve(), s.Result.Sat)

  test("smt2: bit-vectors through the front end"):
    val script = Parser.parse("""
      (declare-const x (_ BitVec 8))
      (assert (= (bvadd x #xFF) #x00))
      (assert (bvult #x00 x))
      (check-sat)""")
    assertEquals(Player.play(script), List(true))

  test("smt2: parsing, playing, printing"):
    val script = Parser.parse("""
      (set-logic QF_UF)
      (declare-sort U 0)
      (declare-fun f (U) U)
      (declare-const a U)
      (assert (= (f (f a)) a))
      (assert (not (= (f (f (f (f a)))) a)))
      (check-sat)""")
    assertEquals(Player.play(script), List(false))
    assertEquals(Parser.parse(Printer.print(script)), script)

  test("cdclt alone"):
    import net.bovel.cdclt.{Lit, Solver as Sat}
    val sat = Sat(numVars = 3)
    sat.addClause(Lit(0), Lit(1))
    sat.addClause(Lit(0, false), Lit(2))
    sat.addClause(Lit(1, false), Lit(2, false))
    val model = sat.solve() match
      case Sat.Result.Sat(model) => model.toList
      case Sat.Result.Unsat | Sat.Result.Unknown => Nil
    assertEquals(model.length, 3)
    assert((model(0) || model(1)) && (!model(0) || model(2)) && (!model(1) || !model(2)))

  test("egraph alone"):
    import net.bovel.egraph.{App, EGraph}
    val eg = EGraph()
    val (f, a, b) = (eg.newClass(), eg.newClass(), eg.newClass())
    val (fa, fb) = (eg.add(App(f, ArraySeq(a))), eg.add(App(f, ArraySeq(b))))
    eg.merge(a, b, reason = 1)
    eg.rebuild()
    assert(eg.find(fa) == eg.find(fb))
    assertEquals(eg.explain(fa, fb).toList, List(1))
    eg.push()
    eg.disequate(fa, fb, reason = 2)
    assertEquals(eg.conflict.map(_.sorted.toList), Some(List(1, 2)))
    eg.pop()
    assertEquals(eg.conflict, None)

  test("lia alone"):
    import net.bovel.lia.Solver as Lia
    val lia = Lia()
    val (x, y) = (lia.newVar(), lia.newVar())
    val s = lia.newSlack(List((BigInt(2), x), (BigInt(2), y)))
    assertEquals(lia.assertLower(s, 3, reason = 0), Lia.Result.Sat)
    assertEquals(lia.assertUpper(s, 3, reason = 1), Lia.Result.Unsat(Set(0, 1)))
