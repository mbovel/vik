package net.bovel.vik

import scala.util.Random

import Formula.*
import Term.*

/** The solver on linear integer arithmetic, alone and with Boolean structure.
 */
class LiaSolverTest extends munit.FunSuite:
  val x = Const("x")
  val y = Const("y")
  val z = Const("z")

  def solve(formulas: Formula*): Solver#Result =
    val solver = Solver()
    formulas.foreach(solver.assert)
    solver.solve()

  def isSat(formulas: Formula*): Boolean = solve(formulas*).toString == "Sat"

  test("bounds"):
    assert(isSat(x <= Num(3), x >= Num(3)))
    assert(!isSat(x <= Num(3), x >= Num(4)))
    assert(!isSat(x < Num(3), x > Num(2)))
    assert(!isSat(x + y <= Num(1), x >= Num(1), y >= Num(1)))
    assert(isSat(x + y <= Num(2), x >= Num(1), y >= Num(1)))

  test("integrality"):
    assert(!isSat(IntEq(x * 2, Num(1)))) // 2x = 1
    assert(!isSat(IntEq(x + y * 2, Num(1)), IntEq(x, y * 2))) // x + 2y = 1, x = 2y
    assert(isSat(IntEq(x + y * 2, Num(1)), x >= y * 2))
    assert(isSat(IntDistinct(List(x, y, z)), x >= Num(0), z <= Num(2)))
    assert(!isSat(
      IntDistinct(List(x, y, z)),
      x >= Num(0),
      x <= Num(1),
      y >= Num(0),
      y <= Num(1),
      z >= Num(0),
      z <= Num(1)
    ))

  test("Boolean structure"):
    assert(isSat(Or(List(x <= Num(0), x >= Num(5))), x >= Num(1)))
    assert(!isSat(Or(List(x <= Num(0), x >= Num(5))), x >= Num(1), x <= Num(4)))
    assert(!isSat(Implies(x > Num(0), y > x), Implies(Not(x > Num(0)), y > Num(10)), y <= Num(0)))
    assert(isSat(Implies(x > Num(0), y > x), y <= Num(0), x <= Num(0)))

  test("with uninterpreted functions on a disjoint sort"):
    val a = Const("a")
    val b = Const("b")
    val f = Const("f")
    assert(!isSat(App(f, List(a)) !== App(f, List(b)), a === b, x <= y))
    assert(isSat(App(f, List(a)) !== App(f, List(b)), x <= y, y <= x))

  /** Evaluates a formula over integers under `values`. */
  def eval(f: Formula, values: Map[Term, BigInt]): Boolean = f match
    case True => true
    case False => false
    case Not(a) => !eval(a, values)
    case And(as) => as.forall(eval(_, values))
    case Or(as) => as.exists(eval(_, values))
    case Implies(a, b) => !eval(a, values) || eval(b, values)
    case Iff(a, b) => eval(a, values) == eval(b, values)
    case Ite(c, t, e) => if eval(c, values) then eval(t, values) else eval(e, values)
    case Le(l, r) => eval(l, values) <= eval(r, values)
    case Eq(_, _) | Pred(_) => throw IllegalArgumentException("not arithmetic")

  def eval(t: Term, values: Map[Term, BigInt]): BigInt = t match
    case Num(v) => v
    case Linear(terms, c) => terms.map((a, u) => a * eval(u, values)).sum + c
    case _ => values(t)

  test("random formulas against brute force"):
    val rnd = Random(3)
    val vars = List(x, y, z)
    def term(): Term = Term.linear(
      vars.filter(_ => rnd.nextBoolean()).map(v => (BigInt(rnd.nextInt(5) - 2), v)),
      rnd.nextInt(5) - 2
    )
    def atom(): Formula = rnd.nextInt(4) match
      case 0 => term() <= term()
      case 1 => term() < term()
      case 2 => IntEq(term(), term())
      case _ => term() >= Num(rnd.nextInt(3))
    def formula(depth: Int): Formula =
      if depth == 0 then atom()
      else
        rnd.nextInt(4) match
          case 0 => And(List(formula(depth - 1), formula(depth - 1)))
          case 1 => Or(List(formula(depth - 1), formula(depth - 1)))
          case 2 => Not(formula(depth - 1))
          case _ => Implies(formula(depth - 1), formula(depth - 1))
    val box = 2
    var sat = 0
    for round <- 0 until 200 do
      val bounds = vars.flatMap(v => List(v >= Num(-box), v <= Num(box)))
      val f = formula(2)
      val expected = (for a <- -box to box; b <- -box to box; c <- -box to box
      yield Map[Term, BigInt](x -> a, y -> b, z -> c)).exists(eval(f, _))
      assertEquals(isSat((f :: bounds)*), expected, s"round $round: $f")
      if expected then sat += 1
    assert(sat > 20 && sat < 195, sat) // both answers exercised

  test("push and pop"):
    val solver = Solver()
    solver.assert(x + y <= Num(3))
    solver.assert(x >= Num(1))
    assertEquals(solver.solve(), solver.Result.Sat)
    solver.push()
    solver.assert(y >= Num(3))
    assertEquals(solver.solve(), solver.Result.Unsat)
    solver.pop()
    assertEquals(solver.solve(), solver.Result.Sat)
