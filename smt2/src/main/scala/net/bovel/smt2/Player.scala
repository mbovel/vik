package net.bovel.smt2

import scala.collection.mutable.ArrayBuffer

import net.bovel.smt.Solver

/** Runs a [[Script]] on a [[Solver]]: `assert`, `push`, `pop` and `check-sat`
 *  become calls, and the responses are returned: `sat`, `unsat` or `unknown`
 *  per `check-sat`, and the `:reason-unknown` of `get-info` — `resourceout`
 *  when the fuel is spent (`:rlimit`, or `fuel` here), `timeout` when the
 *  `:timeout` (milliseconds) passed, `canceled` when the caller stopped it.
 */
object Player:
  def play(script: Script, solver: Solver = Solver()): List[Boolean] =
    answers(script, solver).map(_.getOrElse(throw IllegalStateException("unknown without a budget")))

  /** The answer of each `check-sat`: `Some(true)` for sat, `None` for unknown
   *  (the `fuel` of each `check-sat` is spent, see `Solver.solve`).
   */
  def answers(script: Script, solver: Solver = Solver(), fuel: Long = Long.MaxValue): List[Option[Boolean]] =
    run(script, solver, fuel).collect {
      case "sat" => Some(true)
      case "unsat" => Some(false)
      case "unknown" => None
    }

  /** The responses of the script, in order. */
  def run(script: Script, solver: Solver = Solver(), fuel: Long = Long.MaxValue): List[String] =
    val responses = ArrayBuffer.empty[String]
    var current = solver
    var rlimit = fuel
    var timeout = Option.empty[Long] // milliseconds
    var reason = Option.empty[String]
    def answer(): Unit =
      val s = current
      val deadline = timeout.map(ms => System.nanoTime() + ms * 1000000)
      var left = rlimit
      var late = false
      val result = s.solve(() =>
        left -= 1
        late = deadline.exists(_ < System.nanoTime())
        left >= 0 && !late
      )
      reason = result match
        case s.Result.Unknown(_) => Some(if late then "timeout" else if left < 0 then "resourceout" else "canceled")
        case _ => None
      responses +=
        (result match
          case s.Result.Sat => "sat"
          case s.Result.Unsat => "unsat"
          case s.Result.Unknown(_) => "unknown")
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
        case Command.CheckSat => answer()
        case Command.CheckSatAssuming(assumptions) =>
          current.push()
          assumptions.foreach(current.assert)
          answer()
          current.pop()
        case Command.ResetAssertions =>
          current = Solver(simplify = solver.simplify)
          declarations.foreach(declare)
        case Command.Reset =>
          current = Solver(simplify = solver.simplify)
          declarations.clear()
        case Command.SetOption(":rlimit", value) => rlimit = value.toLong
        case Command.SetOption(":timeout", value) => timeout = Some(value.toLong)
        case Command.GetInfo(":reason-unknown") => responses += s"((:reason-unknown ${reason.getOrElse("incomplete")}))"
        case _ => ()
    responses.toList
