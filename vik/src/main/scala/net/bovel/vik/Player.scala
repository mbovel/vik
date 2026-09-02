package net.bovel.vik
import scala.collection.mutable.ArrayBuffer

import net.bovel.smtlib as s2

/** Runs a script on a [[Solver]]: `assert`, `push`, `pop` and `check-sat`
 *  become calls, and the responses are returned: `sat`, `unsat` or `unknown`
 *  per `check-sat`, and the `:reason-unknown` of `get-info` — `resourceout`
 *  when the fuel is spent (`:rlimit`, or `fuel` here), `timeout` when the
 *  `:timeout` (milliseconds) passed, `canceled` when the caller stopped it. A
 *  surface script (`net.bovel.smtlib`) is [[Lowering lowered]] first; a lowered
 *  [[Script]] plays as it is.
 */
object Player:
  private def valueText(m: Model, item: Formula | Term): String = item match
    case f: Formula => if m.holds(f) then "true" else "false"
    case t: Term => Printer.print(m.value(t))

  /** The model as SMT-LIB text: a define-fun per declared constant and per
   *  declared function (an ite chain over its graph); the interpreted symbols
   *  of the datatypes stay out.
   */
  private def modelText(m: Model, sorts: Map[String, String], interpreted: Set[String]): String =
    val constants = m.constants.toList.filterNot((n, _) => interpreted(n)).sortBy(_._1).map((n, v) =>
      s"(define-fun $n () ${sorts.getOrElse(n, "?")} ${Printer.print(v)})"
    )
    val functions = m.functions.toList.filterNot((n, _) => interpreted(n)).sortBy(_._1).map { (n, f) =>
      val params = f.parameters.zipWithIndex.map((s, i) => s"(x!$i $s)").mkString(" ")
      val body = f.rows.foldRight(Printer.print(f.default)) { case ((args, r), els) =>
        val cond = args.zipWithIndex.map((a, i) => s"(= x!$i ${Printer.print(a)})") match
          case single :: Nil => single
          case cs => cs.mkString("(and ", " ", ")")
        s"(ite $cond ${Printer.print(r)} $els)"
      }
      s"(define-fun $n ($params) ${f.result} $body)"
    }
    (constants ++ functions).mkString("(", " ", ")")
  def play(script: s2.Script, solver: Solver = Solver()): List[Boolean] = play(Lowering(script), solver)

  /** The answer of each `check-sat`: `Some(true)` for sat, `None` for unknown
   *  (the `fuel` of each `check-sat` is spent, see `Solver.solve`).
   */
  def answers(script: s2.Script, solver: Solver = Solver(), fuel: Long = Long.MaxValue): List[Option[Boolean]] =
    answers(Lowering(script), solver, fuel)

  /** The responses of the script, in order. */
  def run(script: s2.Script, solver: Solver = Solver(), fuel: Long = Long.MaxValue): List[String] =
    run(Lowering(script), solver, fuel)

  def play(lowered: Script, solver: Solver): List[Boolean] =
    answers(lowered, solver, Long.MaxValue).map(_.getOrElse(throw IllegalStateException("unknown without a budget")))

  def answers(lowered: Script, solver: Solver, fuel: Long): List[Option[Boolean]] =
    run(lowered, solver, fuel).collect {
      case "sat" => Some(true)
      case "unsat" => Some(false)
      case "unknown" => None
    }

  def run(lowered: Script, solver: Solver, fuel: Long): List[String] =
    val responses = ArrayBuffer.empty[String]
    // Models cost a walk per sat: taken only when the script asks for them.
    val wantsModels = lowered.commands.exists {
      case Command.GetModel | Command.GetValue(_) => true
      case Command.SetOption(":produce-models", "true") => true
      case _ => false
    }
    var current = solver
    current.produceModels = wantsModels
    // The assertion stack as asserted, one buffer per level: what get-assertions prints.
    val asserted = ArrayBuffer(ArrayBuffer.empty[Formula])
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
    val sorts = scala.collection.mutable.HashMap.empty[String, String] // declared constants, for get-model
    val interpreted = scala.collection.mutable.HashSet.empty[String] // datatype members: not printed
    def declare(command: Command): Unit = command match
      case Command.DeclareFun(name, argSorts, sort) =>
        current.declare(name, argSorts, sort)
        if argSorts.isEmpty then sorts(name) = sort
      case Command.DeclareDatatypes(datatypes) =>
        datatypes.foreach(current.declare)
        for d <- datatypes; c <- d.constructors do
          interpreted += c.name
          interpreted += c.tester
          interpreted ++= c.selectors.map(_._1)
      case _ => ()
    for command <- lowered.commands do
      command match
        case Command.DeclareFun(_, _, _) | Command.DeclareDatatypes(_) =>
          declarations += command
          declare(command)
        case Command.Assert(formula) =>
          asserted.last += formula
          current.assert(formula)
        case Command.AssertNamed(name, formula) =>
          asserted.last += formula
          current.assert(formula, name)
        case Command.GetUnsatCore => responses += current.unsatCore.mkString("(", " ", ")")
        case Command.GetAssertions =>
          responses += asserted.flatten.map(Printer.print).mkString("(", " ", ")")
        case Command.GetValue(items) =>
          responses +=
            (current.model() match
              case Some(m) => items.map((src, it) => s"($src ${valueText(m, it)})").mkString("(", " ", ")")
              case None => "(error \"no model\")")
        case Command.GetModel =>
          responses +=
            (current.model() match
              case Some(m) => modelText(m, sorts.toMap, interpreted.toSet)
              case None => "(error \"no model\")")
        case Command.Push(n) =>
          for _ <- 0 until n do
            current.push()
            asserted += ArrayBuffer.empty
        case Command.Pop(n) =>
          for _ <- 0 until n do current.pop()
          asserted.dropRightInPlace(n min (asserted.length - 1))
        case Command.CheckSat => answer()
        case Command.CheckSatAssuming(assumptions) =>
          current.push()
          assumptions.foreach(current.assert)
          answer()
          current.pop()
        case Command.ResetAssertions =>
          asserted.clear(); asserted += ArrayBuffer.empty
          current = Solver(simplify = solver.simplify)
          current.produceModels = wantsModels
          declarations.foreach(declare)
        case Command.Reset =>
          asserted.clear(); asserted += ArrayBuffer.empty
          current = Solver(simplify = solver.simplify)
          current.produceModels = wantsModels
          declarations.clear()
        case Command.SetOption(":rlimit", value) => rlimit = value.toLong
        case Command.SetOption(":timeout", value) => timeout = Some(value.toLong)
        case Command.GetInfo(":reason-unknown") => responses += s"((:reason-unknown ${reason.getOrElse("incomplete")}))"
        case _ => ()
    responses.toList
