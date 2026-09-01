package net.bovel.vik

import net.bovel.smt2.{Parser, Smt2Error, Unsupported}
import Formula.*
import Term.*

/** The lowering, shape by shape: names to de Bruijn indices, redexes reduced,
 *  `ite` named, and the fragment line — what parses but the solver does not
 *  speak — drawn with a message.
 */
class LoweringTest extends munit.FunSuite:
  def lowered(script: String): List[Formula] =
    Lowering(Parser.parse(script)).commands.collect { case Command.Assert(f) => f }

  test("lambda names become de Bruijn indices"):
    val printed = Printer.print(Lowering(Parser.parse("""
      (declare-fun f (Int Int) Int)
      (declare-const h (-> Int (-> Int Int)))
      (assert (= h (lambda ((y Int)) (lambda ((z Int)) (f z y)))))""")))
    assert(printed.contains("(lambda ((%0 Int)) (lambda ((%1 Int)) (f %1 %0)))"), printed)

  test("syntactic redexes are reduced"):
    val asserts = lowered("""
      (declare-fun f (Int) Int)
      (declare-const c Int)
      (assert (= c ((lambda ((x Int)) (+ (f x) 1)) 3)))
      (assert (= ((lambda ((g (-> Int Int)) (y Int)) (g (g y))) (lambda ((z Int)) (f z)) c) 7))""")
    val f = Const("f")
    assertEquals(asserts(0), IntEq(Const("c"), Term.linear(List((BigInt(1), App(f, List(Num(3))))), 1)))
    assertEquals(asserts(1), IntEq(App(f, List(App(f, List(Const("c"))))), Num(7)))

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
      "(declare-const a (_ BitVec 8)) (declare-const b (_ BitVec 8)) (assert (= (bvor a b) a))"
    ))
