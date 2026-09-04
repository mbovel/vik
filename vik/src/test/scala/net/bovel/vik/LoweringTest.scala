package net.bovel.vik

import net.bovel.smtlib.{Parser, Smt2Error, Unsupported}
import Formula.*
import Term.*

/** The lowering, shape by shape: names to de Bruijn indices, redexes reduced,
 *  `ite` named, and the fragment line — what parses but the solver does not
 *  speak — drawn with a message.
 */
class LoweringTest extends munit.FunSuite:
  def lowered(script: String): List[Formula] =
    Lowering(Parser.parse(script)).commands.collect { case Command.Assert(f) => f }

  test("a term-level ite becomes a defined constant before its assertion"):
    val commands = Lowering(Parser.parse(
      "(declare-const p Bool) (declare-const x Int) (declare-const y Int) (assert (= (ite p x y) 2))"
    )).commands
    assert(commands.exists { case Command.DeclareFun("ite!1", Nil, "Int") => true; case _ => false })
    val asserts = commands.collect { case Command.Assert(f) => f }
    assertEquals(asserts(0), Implies(Pred(Const("p")), IntEq(Const("ite!1"), Const("x"))))

  test("the integer normal form: chains, linear combinations"):
    val asserts = lowered("""
      (declare-const x Int) (declare-const y Int)
      (assert (<= (+ (* 2 x) y (- 3)) 5))
      (assert (< 0 x y))
      (assert (distinct x y 7))""")
    val (x, y) = (Const("x"), Const("y"))
    assertEquals(asserts(0), Le(Linear(List((2, x), (1, y)), -3), Num(5)))
    assertEquals(asserts(1), And(List(Lt(Num(0), x), Lt(x, y)))) // chained, pairwise
    assertEquals(asserts(2), IntDistinct(List(x, y, Num(7))))

  test("outside the fragment: nonlinear multiplication, integer division"):
    intercept[Smt2Error](lowered("(declare-const x Int) (assert (<= (* x x) 1))"))
    intercept[Unsupported](lowered("(declare-const x Int) (assert (= (div x 2) 1))"))
    intercept[Unsupported](lowered(
      "(declare-const a (_ BitVec 8)) (declare-const b (_ BitVec 8)) (assert (= (bvudiv a b) a))"
    ))

  test("to_real is the identity, to_int the pinned floor"):
    val commands = Lowering(Parser.parse(
      "(declare-const n Int) (declare-const x Real) (assert (= x (to_real n))) (assert (= (to_int x) 2))"
    )).commands
    val asserts = commands.collect { case Command.Assert(f) => f }
    assertEquals(asserts(0), IntEq(Const("x"), Const("n"))) // to_real vanishes: sorts carry the difference
    assert(commands.exists { case Command.DeclareFun("floor!1", Nil, "Int") => true; case _ => false })
    assertEquals(asserts(1), Le(Const("floor!1"), Const("x")))
    assertEquals(asserts(2), Less(Const("x"), Linear(List((BigInt(1), Const("floor!1"))), 1)))
    assertEquals(asserts(3), IntEq(Const("floor!1"), Num(2)))

  test("the casts fold on literals"):
    assertEquals(lowered("(assert (is_int 2.0))"), List(True))
    assertEquals(lowered("(assert (not (is_int 2.5)))").last, Not(False))
    assertEquals(lowered("(assert (= (to_int 2.5) 2))").last, IntEq(Num(2), Num(2)))
    assertEquals(lowered("(assert (= (to_int (- 5.5)) (- 6)))").last, IntEq(Num(-6), Num(-6)))

  test("the bit-vector conversions: identity, sign split, pinned modulo"):
    assertEquals(
      lowered("(declare-const x (_ BitVec 8)) (declare-const n Int) (assert (= (ubv_to_int x) n))").last,
      IntEq(Const("x"), Const("n")) // ubv_to_int vanishes: the lowered bit-vector is its unsigned value
    )
    assertEquals(lowered("(assert (= (sbv_to_int #xFF) (- 1)))").last, IntEq(Num(-1), Num(-1)))
    assertEquals(lowered("(assert (= (sbv_to_int #x7F) 127))").last, IntEq(Num(127), Num(127)))
    assertEquals(lowered("(assert (= ((_ int_to_bv 8) 300) #x2C))").last, IntEq(Num(44), Num(44)))
    assertEquals(lowered("(assert (= ((_ int_to_bv 8) (- 1)) #xFF))").last, IntEq(Num(255), Num(255)))
    val asserts = lowered("(declare-const n Int) (assert (= ((_ int_to_bv 8) n) #x2A))")
    assertEquals(asserts.last, IntEq(Const("bv!1"), Num(42)))
    assert(asserts.contains(IntEq(Const("n"), Linear(List((1, Const("bv!1")), (256, Const("k!2"))), 0))), asserts)
