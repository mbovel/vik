package net.bovel.smt

import net.bovel.smt.Formula.*
import net.bovel.smt.Term.*

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
