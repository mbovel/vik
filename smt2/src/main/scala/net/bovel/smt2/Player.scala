package net.bovel.smt2

import scala.collection.mutable.ArrayBuffer

import net.bovel.smt.Solver

/** Runs a [[Script]] on a [[Solver]]: `assert`, `push`, `pop` and `check-sat`
 *  become calls, and the results of the `check-sat`s are returned (`true` for
 *  sat).
 */
object Player:
  def play(script: Script, solver: Solver = Solver()): List[Boolean] =
    answers(script, solver).map(_.getOrElse(throw IllegalStateException("unknown without a budget")))

  /** The answer of each `check-sat`: `Some(true)` for sat, `None` for unknown
   *  (the `fuel` of each `check-sat` is spent, see `Solver.solve`).
   */
  def answers(script: Script, solver: Solver = Solver(), fuel: Long = Long.MaxValue): List[Option[Boolean]] =
    val results = ArrayBuffer.empty[Option[Boolean]]
    var current = solver
    def answer(): Option[Boolean] =
      val s = current
      s.solve(fuel) match
        case s.Result.Sat => Some(true)
        case s.Result.Unsat => Some(false)
        case s.Result.Unknown => None
    val declarations = ArrayBuffer.empty[Command] // replayed by `reset-assertions`
    def declare(command: Command): Unit = command match
      case Command.DeclareFun(name, argSorts, sort) => current.declare(name, argSorts, sort)
      case Command.DeclareDatatypes(datatypes) => datatypes.foreach(current.declare)
      case _ => ()
    for command <- script.commands do
      command match
        case Command.DeclareFun(_, _, _) | Command.DeclareDatatypes(_) =>
          declarations += command
          declare(command)
        case Command.Assert(formula) => current.assert(formula)
        case Command.Push(n) => for _ <- 0 until n do current.push()
        case Command.Pop(n) => for _ <- 0 until n do current.pop()
        case Command.CheckSat => results += answer()
        case Command.CheckSatAssuming(assumptions) =>
          current.push()
          assumptions.foreach(current.assert)
          results += answer()
          current.pop()
        case Command.ResetAssertions =>
          current = Solver(simplify = solver.simplify)
          declarations.foreach(declare)
        case Command.Reset =>
          current = Solver(simplify = solver.simplify)
          declarations.clear()
        case _ => ()
    results.toList
