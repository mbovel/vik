package net.bovel.vik

import Formula.*
import Term.*

/** Lambdas through the API: a function symbol equal to a lambda, applied. */
class LambdaTest extends munit.FunSuite:
  test("beta-reduction and alpha-equivalence"):
    val solver = Solver()
    solver.declare("g", List("Int"), "Int")
    val g = Const("g")
    val increment = Lambda(List("Int"), Term.linear(List((BigInt(1), Var(0))), 1)) // λx. x + 1
    solver.assert(Eq(g, increment))
    solver.assert(Eq(increment, Lambda(List("Int"), Term.linear(List((BigInt(1), Var(0))), 1)))) // the same term
    solver.push()
    solver.assert(Not(IntEq(App(g, List(Num(41))), Num(42))))
    assertEquals(solver.solve(), solver.Result.Unsat)
    solver.pop()
    solver.assert(IntEq(App(g, List(Num(41))), Num(42)))
    assertEquals(solver.solve(), solver.Result.Sat)

  test("Term.app reduces closed redexes, to normal form"):
    val f = Const("f")
    val increment = Lambda(List("Int"), Term.linear(List((BigInt(1), Var(0))), 1))
    assertEquals(Term.app(increment, List(Num(41))), Num(42))
    val twice = Lambda(List("(-> Int Int)", "Int"), App(Var(0), List(App(Var(0), List(Var(1))))))
    assertEquals(Term.app(twice, List(increment, Num(40))), Num(42))
    assertEquals(Term.app(f, List(Num(1))), App(f, List(Num(1)))) // not a redex
    assertEquals(Term.app(increment, List(Var(0))), App(increment, List(Var(0)))) // an open argument: kept

  test("Term.app flattens applications and reduces partial applications of lambdas"):
    val f = Const("f")
    assertEquals(Term.app(App(f, List(Num(1))), List(Num(2))), App(f, List(Num(1), Num(2))))
    val minus = Lambda(List("Int", "Int"), Term.linear(List((BigInt(1), Var(0)), (BigInt(-1), Var(1)))))
    assertEquals(Term.app(minus, List(Num(5))), Lambda(List("Int"), Term.linear(List((BigInt(-1), Var(0))), 5)))
    assertEquals(Term.app(Term.app(minus, List(Num(5))), List(Num(3))), Num(2))
    assertEquals(Term.app(minus, List(Num(5), Num(3))), Num(2))
