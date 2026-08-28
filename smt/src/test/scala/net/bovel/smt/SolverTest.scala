package net.bovel.smt

import Formula.*
import Term.*

class SolverTest extends munit.FunSuite:
  val a = Const("a")
  val b = Const("b")
  val c = Const("c")
  val f = Const("f")
  def fa(t: Term) = App(f, List(t))
  def p(t: Term) = Pred(App(Const("p"), List(t)))

  def solve(formulas: Formula*): Solver#Result =
    val s = Solver()
    for f <- formulas do s.assert(f)
    s.solve()

  def isSat(formulas: Formula*): Boolean = solve(formulas*) match
    case r: Solver#Result => r.toString == "Sat"

  test("trivial"):
    assert(isSat())
    assert(isSat(True))
    assert(!isSat(False))
    assert(isSat(a === b))
    assert(!isSat(a === b, a !== b))

  test("transitivity and congruence"):
    assert(!isSat(a === b, b === c, a !== c))
    assert(!isSat(a === b, fa(a) !== fa(b)))
    assert(isSat(a === b, fa(a) !== fa(c)))
    assert(!isSat(a === b, p(a), !p(b)))

  test("boolean structure"):
    assert(isSat((a === b) || (b === c), a !== c, a !== b, (b === c) && (a !== c)))
    assert(!isSat((a === b) || (b === c), a !== b, b !== c))
    assert(isSat((a === b) ==> (fa(a) === fa(b))))
    assert(!isSat(Not((a === b) ==> (fa(a) === fa(b)))))
    assert(isSat(Ite(a === b, fa(a) === c, fa(b) === c)))
    assert(!isSat((a === b) <=> (a !== b)))

  test("diamonds"):
    // x0 = xn through n diamonds, each x_i = y_i = x_{i+1} or x_i = z_i = x_{i+1}; then x0 != xn.
    val n = 6
    val x = Array.tabulate(n + 1)(i => Const(s"x$i"))
    val diamonds = (0 until n).map { i =>
      val y = Const(s"y$i")
      val z = Const(s"z$i")
      ((x(i) === y) && (y === x(i + 1))) || ((x(i) === z) && (z === x(i + 1)))
    }
    assert(isSat(diamonds*))
    assert(!isSat((diamonds :+ (x(0) !== x(n)))*))

  test("push and pop"):
    val s = Solver()
    s.assert(a === b)
    s.push()
    s.assert(a !== b)
    assertEquals(s.solve().toString, "Unsat")
    s.pop()
    assertEquals(s.solve().toString, "Sat")
    intercept[IllegalArgumentException](s.pop())

  test("random formulas over predicates agree with brute force"):
    val rnd = scala.util.Random(9)
    val ps = Array.tabulate(5)(i => Pred(Const(s"q$i")))
    def random(depth: Int): Formula =
      if depth == 0 then ps(rnd.nextInt(ps.length))
      else
        rnd.nextInt(6) match
          case 0 => Not(random(depth - 1))
          case 1 => And(List.fill(2 + rnd.nextInt(2))(random(depth - 1)))
          case 2 => Or(List.fill(2 + rnd.nextInt(2))(random(depth - 1)))
          case 3 => Implies(random(depth - 1), random(depth - 1))
          case 4 => Iff(random(depth - 1), random(depth - 1))
          case _ => Ite(random(depth - 1), random(depth - 1), random(depth - 1))
    def eval(f: Formula, m: Array[Boolean]): Boolean = f match
      case True => true
      case False => false
      case Not(g) => !eval(g, m)
      case And(gs) => gs.forall(eval(_, m))
      case Or(gs) => gs.exists(eval(_, m))
      case Implies(l, r) => !eval(l, m) || eval(r, m)
      case Iff(l, r) => eval(l, m) == eval(r, m)
      case Ite(c, t, e) => if eval(c, m) then eval(t, m) else eval(e, m)
      case Pred(Const(name)) => m(name.drop(1).toInt)
      case _ => throw IllegalArgumentException(f.toString)
    var sats = 0
    for _ <- 0 until 200 do
      val formulas = List.fill(1 + rnd.nextInt(3))(random(3))
      val expected = (0 until 32).exists(bits =>
        formulas.forall(eval(_, Array.tabulate(5)(i => (bits & (1 << i)) != 0)))
      )
      assertEquals(isSat(formulas*), expected, formulas.toString)
      if expected then sats += 1
    assert(sats > 50)
