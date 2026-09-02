package net.bovel.vik
import scala.collection.mutable.ArrayBuffer

import net.bovel.smtlib as s2

/** A solver with the language attached: one parser, one lowering and one
 *  [[Solver]] kept alive together, so a script arrives piece by piece — as
 *  text, as `net.bovel.smtlib` trees or as lowered [[Command]]s — each piece in
 *  the scope the earlier ones declared. The responses come back as SMT-LIB
 *  text, one per command that answers (`check-sat`, `get-model`, ...), and
 *  `(get-info :reason-unknown)` says `resourceout` when the fuel is spent
 *  (`:rlimit`, or `fuel` here), `timeout` when the `:timeout` (milliseconds)
 *  passed, `canceled` when the caller stopped it.
 *
 *  {{{
 *  val s = Session()
 *  s.run("(declare-const x Int) (declare-const y Int)")
 *  s.assert("(< x y)")
 *  s.checkSat() // Some(true)
 *  }}}
 */
final class Session(initial: Solver = Solver(), fuel: Long = Long.MaxValue):
  private val parser = s2.Parser()
  private val lowerer = Lowerer()
  private var current = initial

  /** The solver underneath (`reset` replaces it). */
  def solver: Solver = current

  // The assertion stack as asserted, one buffer per level: what get-assertions prints.
  private val asserted = ArrayBuffer(ArrayBuffer.empty[Formula])
  private val declarations = ArrayBuffer.empty[Command] // replayed by `reset-assertions`
  private var rlimit = fuel
  private var timeout = Option.empty[Long] // milliseconds
  private var reason = Option.empty[String]

  /** Runs SMT-LIB text, any commands, and answers it. */
  def run(text: String): List[String] = run(parser.parse(text))
  def run(script: s2.Script): List[String] = play(lowerer.commands(script.commands))
  def assert(text: String): Unit = run(s"(assert $text)")
  def assert(formula: s2.Formula): Unit = run(s2.Script(List(s2.Command.Assert(formula))))
  def push(): Unit = play(List(Command.Push(1)))
  def pop(): Unit = play(List(Command.Pop(1)))

  /** `check-sat`: `Some(true)` for sat, `None` for unknown. */
  def checkSat(): Option[Boolean] = play(List(Command.CheckSat)).head match
    case "sat" => Some(true)
    case "unsat" => Some(false)
    case _ => None

  /** The model of the last `check-sat`, when it was sat and the solver
   *  `produceModels` — set by `(set-option :produce-models true)`, or by a
   *  `get-model` or `get-value` anywhere in the commands given so far.
   */
  def model(): Option[Model] = current.model()
  def unsatCore: List[String] = current.unsatCore

  private def answer(): String =
    val s = current
    val deadline = timeout.map(ms => System.nanoTime() + ms * 1000000)
    var left = rlimit
    var late = false
    reason = None
    s.solve(() =>
      left -= 1; late = deadline.exists(_ < System.nanoTime()); left >= 0 && !late
    ) match
      case s.Result.Sat => "sat"
      case s.Result.Unsat => "unsat"
      case s.Result.Unknown(_) =>
        reason = Some(if late then "timeout" else if left < 0 then "resourceout" else "canceled")
        "unknown"

  private def declare(command: Command): Unit = command match
    case Command.DeclareFun(name, argSorts, sort) => current.declare(name, argSorts, sort)
    case Command.DeclareDatatypes(datatypes) => datatypes.foreach(current.declare)
    case _ => ()

  private def withModel(text: Model => String): String = current.model().map(text).getOrElse("(error \"no model\")")

  private def restart(): Unit =
    asserted.clear(); asserted += ArrayBuffer.empty
    val produceModels = current.produceModels
    current = Solver(simplify = current.simplify)
    current.produceModels = produceModels

  /** Plays lowered commands. */
  def play(commands: List[Command]): List[String] =
    val responses = ArrayBuffer.empty[String]
    // Models cost a walk per sat: taken only once the script asks for them.
    if commands.exists {
        case Command.GetModel | Command.GetValue(_) | Command.SetOption(":produce-models", "true") => true
        case _ => false
      }
    then current.produceModels = true
    for command <- commands do
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
            withModel(m => items.map((src, it) => s"($src ${Player.valueText(m, it)})").mkString("(", " ", ")"))
        case Command.GetModel => responses += withModel(Player.modelText(_, current))
        case Command.Push(n) =>
          for _ <- 0 until n do
            current.push()
            asserted += ArrayBuffer.empty
        case Command.Pop(n) =>
          for _ <- 0 until n do current.pop()
          asserted.dropRightInPlace(n min (asserted.length - 1))
        case Command.CheckSat => responses += answer()
        case Command.CheckSatAssuming(assumptions) =>
          current.push()
          assumptions.foreach(current.assert)
          responses += answer()
          current.pop()
        case Command.ResetAssertions =>
          restart()
          declarations.foreach(declare)
        case Command.Reset =>
          restart()
          declarations.clear()
        case Command.SetOption(":rlimit", value) => rlimit = value.toLong
        case Command.SetOption(":timeout", value) => timeout = Some(value.toLong)
        case Command.GetInfo(":reason-unknown") => responses += s"((:reason-unknown ${reason.getOrElse("incomplete")}))"
        case _ => ()
    responses.toList
