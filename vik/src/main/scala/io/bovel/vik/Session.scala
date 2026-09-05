package io.bovel.vik
import scala.collection.mutable.ArrayBuffer

import io.bovel.smtlib as s2

/** A solver with the language attached: one parser, one lowering and one
 *  [[Solver]] kept alive together, so a script arrives piece by piece — as
 *  text, as `io.bovel.smtlib` trees or as lowered [[Command]]s — each piece in
 *  the scope the earlier ones declared. The responses come back as SMT-LIB
 *  text, one per command that answers (`check-sat`, `get-model`, ...), and
 *  `(get-info :reason-unknown)` says `resourceout` when the fuel is spent
 *  (`:rlimit`, or `fuel` here), `timeout` when the `:timeout` (milliseconds)
 *  passed, `canceled` when the caller stopped it. A whole script in a session
 *  of its own is [[Session.run]], or [[Session.answers]] for just its
 *  `check-sat`s.
 *
 *  {{{
 *  val s = Session()
 *  s.run("(declare-const x Int) (declare-const y Int)")
 *  s.assert("(< x y)")
 *  s.checkSat() // Some(true)
 *  }}}
 */
final class Session(
    initial: Solver = Solver(),
    fuel: Long = Long.MaxValue,
    timeoutMs: Option[Long] = None,
    deadline: Option[Long] = None // a point in `System.nanoTime`: no check-sat searches past it
):
  private val parser = s2.Parser()
  private val lowerer = Lowerer()
  private var current = initial
  current.produceModels = false // a walk per sat: paid only once the commands ask (see play)

  /** The solver underneath (`reset` replaces it). */
  def solver: Solver = current

  // The assertion stack as asserted, one buffer per level: what get-assertions prints.
  private val asserted = ArrayBuffer(ArrayBuffer.empty[Formula])
  private val declarations = ArrayBuffer.empty[Command] // replayed by `reset-assertions`
  private var rlimit = fuel
  private var timeout = timeoutMs // milliseconds per check-sat, as `(set-option :timeout ...)` sets it
  private var reason = Option.empty[String]

  /** Runs SMT-LIB text, any commands, and answers it. */
  def run(text: String): List[String] = run(parser.parse(text))
  def run(script: s2.Script): List[String] =
    // Models cost a walk per sat, and the script says up front whether it will ask for one.
    if script.commands.exists {
        case s2.Command.GetModel | s2.Command.GetValue(_) => true
        case s2.Command.SetOption(":produce-models", "true") => true
        case _ => false
      }
    then current.produceModels = true
    script.commands.flatMap(command)

  /** One surface command, lowered and played. `get-value` is answered here and
   *  not by the lowered command: the value of a term the lowering gives fresh
   *  constants — every bit-vector operation — is not in the model, which knows
   *  the declared symbols; the lowering computes it from them instead
   *  ([[Lowerer.evaluate]]), and the lowered command answers what it cannot.
   */
  private def command(c: s2.Command): List[String] = c match
    case s2.Command.GetValue(items) =>
      val lowered = lowerer.commands(List(c))
      val computed =
        current.model().map(m => items.map((source, item) => (source, item, lowerer.evaluate(item, m))))
      computed match
        case Some(values) if values.forall(_._3.isDefined) =>
          List(values.map((source, _, value) => s"($source ${value.get})").mkString("(", " ", ")"))
        case _ => play(lowered)
    case _ => play(lowerer.commands(List(c)))
  def assert(text: String): Unit = run(s"(assert $text)")
  def assert(formula: s2.Formula): Unit = run(s2.Script(List(s2.Command.Assert(formula))))
  // Through the lowerer, like the script commands: a `pop` clears the caches whose defining
  // assertions die with the level, and a `pop` that bypassed it left a cached rational constant
  // without its definition (issue 22).
  def push(): Unit = run(s2.Script(List(s2.Command.Push(1))))
  def pop(): Unit = run(s2.Script(List(s2.Command.Pop(1))))

  /** `check-sat`: `Some(true)` for sat, `None` for unknown. */
  def checkSat(): Option[Boolean] = play(List(Command.CheckSat)).head match
    case "sat" => Some(true)
    case "unsat" => Some(false)
    case _ => None

  /** The model of the last `check-sat`, when it was sat and models were asked
   *  for — `(set-option :produce-models true)`, or a `get-model` or `get-value`
   *  anywhere in the commands given so far: a script that never asks pays
   *  nothing for them.
   */
  def model(): Option[Model] = current.model()
  def unsatCore: List[String] = current.unsatCore

  private def answer(): String =
    val s = current
    // The check-sat's own timeout, and the session's deadline: whichever comes first.
    val stop = (timeout.map(ms => System.nanoTime() + ms * 1000000).toList ++ deadline).minOption
    var left = rlimit
    var late = false
    reason = None
    s.solve(() =>
      left -= 1; late = stop.exists(_ < System.nanoTime()); left >= 0 && !late
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

  /** The model with a fresh constant valued by its definition — `k = t`, or
   *  `c ⇒ k = t` when `c` holds — the way the lowering states one for a
   *  term-level `ite`; another shape defines nothing.
   */
  private def define(m: Model, definition: Formula): Model =
    def bind(k: String, t: Term): Model = m.copy(constants = m.constants + (k -> m.value(t)))
    definition match
      case Formula.Implies(c, body) => if m.holds(c) then define(m, body) else m
      case Formula.Eq(Term.Const(k), t) => bind(k, t)
      case Formula.Eq(t, Term.Const(k)) => bind(k, t)
      case Formula.And(List(Formula.Le(Term.Const(k), t), Formula.Le(u, Term.Const(k2)))) if k == k2 && t == u =>
        bind(k, t) // `IntEq`
      case _ => m

  private def valueText(m: Model, item: Formula | Term): String = item match
    case f: Formula => if m.holds(f) then "true" else "false"
    case t: Term => Printer.print(m.value(t))

  /** The model as SMT-LIB text: a define-fun per declared constant and per
   *  declared function (an ite chain over its graph); the interpreted symbols
   *  of the datatypes stay out.
   */
  private def modelText(m: Model): String =
    val sorts = current.declarations.collect { case (n, (Nil, s)) => n -> s }
    val interpreted =
      current.datatypes.flatMap(_.constructors).flatMap(c => c.name :: c.tester :: c.selectors.map(_._1)).toSet
    val constants = m.constants.toList.filterNot((n, _) => interpreted(n)).sortBy(_._1).map((n, v) =>
      s"(define-fun $n () ${s2.Sorts.array(sorts.getOrElse(n, "?"))} ${Printer.print(v)})"
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
        case Command.GetModel | Command.GetValue(_, _) | Command.SetOption(":produce-models", "true") => true
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
        case Command.GetValue(items, definitions) =>
          responses += withModel { m0 =>
            val m = definitions.foldLeft(m0)(define)
            items.map((src, it) => s"($src ${valueText(m, it)})").mkString("(", " ", ")")
          }
        case Command.GetModel => responses += withModel(modelText)
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

object Session:
  /** Runs a whole script in a session of its own: the responses, each
   *  `check-sat` with `fuel` steps and, when given, `timeoutMs` milliseconds,
   *  none of them past `deadline` (a `System.nanoTime` point: the budget of the
   *  whole script, which a script of many check-sats must not multiply). The
   *  search stops by itself at any of the three: nothing has to interrupt it.
   */
  def run(
      script: s2.Script,
      fuel: Long = Long.MaxValue,
      timeoutMs: Option[Long] = None,
      deadline: Option[Long] = None
  ): List[String] =
    Session(fuel = fuel, timeoutMs = timeoutMs, deadline = deadline).run(script)

  /** The answer of each `check-sat` of a script run in a session of its own:
   *  `Some(true)` for sat, `None` for unknown (the `fuel` of the `check-sat` is
   *  spent, or its time, see `Solver.solve`).
   */
  def answers(
      script: s2.Script,
      fuel: Long = Long.MaxValue,
      timeoutMs: Option[Long] = None,
      deadline: Option[Long] = None
  ): List[Option[Boolean]] =
    run(script, fuel, timeoutMs, deadline).collect {
      case "sat" => Some(true)
      case "unsat" => Some(false)
      case "unknown" => None
    }
