package net.bovel.smt2

import scala.collection.mutable.ArrayBuffer

import net.bovel.smt.Solver

/** Runs a [[Script]] on a [[Solver]]: `assert`, `push`, `pop` and `check-sat`
 *  become calls, and the results of the `check-sat`s are returned (`true` for
 *  sat).
 */
object Player:
  def play(script: Script, solver: Solver = Solver()): List[Boolean] =
    val results = ArrayBuffer.empty[Boolean]
    var datatypes = false
    for command <- script.commands do
      command match
        case Command.DeclareDatatypes(_) => datatypes = true
        case Command.CheckSat if datatypes =>
          throw UnsupportedOperationException("datatypes are parsed but not decided yet (see docs/adt-plan.md)")
        case Command.Assert(formula) => solver.assert(formula)
        case Command.Push(n) => for _ <- 0 until n do solver.push()
        case Command.Pop(n) => for _ <- 0 until n do solver.pop()
        case Command.CheckSat => results += (solver.solve() == solver.Result.Sat)
        case _ => ()
    results.toList
