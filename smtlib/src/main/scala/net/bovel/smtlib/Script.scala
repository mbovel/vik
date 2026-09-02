package net.bovel.smtlib

import net.bovel.sexpr.SExpr

/** A command of an SMT-LIB 2 script, restricted to what the supported logics
 *  need.
 */
enum Command:
  case SetLogic(logic: String)
  case SetInfo(key: String, value: String)
  case DeclareSort(name: String, arity: Int)
  case DeclareFun(name: String, argSorts: List[Sort], sort: Sort)
  case DeclareDatatypes(datatypes: List[Datatype])

  /** A macro: its body is parsed against the parameters' sorts, and the
   *  solver's lowering expands each use.
   */
  case DefineFun(name: String, params: List[(String, Sort)], sort: Sort, body: Term | Formula)
  case Assert(formula: Formula)

  /** `(assert (! formula :named name))`: an assertion that [[GetUnsatCore]] can
   *  name.
   */
  case AssertNamed(name: String, formula: Formula)
  case CheckSat

  /** `check-sat-assuming`: `check-sat` with the assumptions asserted in a level
   *  of their own.
   */
  case CheckSatAssuming(assumptions: List[Formula])
  case Push(levels: Int)
  case Pop(levels: Int)

  /** `reset-assertions`: the assertions and levels go, the declarations stay.
   */
  case ResetAssertions

  /** `reset`: everything goes. */
  case Reset
  case GetModel

  /** `get-value`: each item as written (for the echo) and as translated. */
  case GetValue(items: List[(String, Formula | Term)])
  case GetUnsatCore

  /** `get-assertions`: the current assertion stack, as the solver holds it —
   *  after the lowerings (bit-vectors to integers, rational literals to
   *  constants).
   */
  case GetAssertions
  case Exit

  /** `set-option`: `:rlimit` (the fuel of each `check-sat`) and `:timeout`
   *  (milliseconds) matter to the player.
   */
  case SetOption(key: String, value: String)

  /** `get-info`: `:reason-unknown` is answered by the player. */
  case GetInfo(key: String)

  /** A command with no effect on solving (`get-model`, `echo`, ...).
   */
  case Other(sexpr: SExpr)

final case class Script(commands: List[Command]):
  /** The `:status` from `set-info`, if any: `Some(true)` for sat. */
  def expectedStatus: Option[Boolean] = commands.collectFirst {
    case Command.SetInfo(":status", "sat") => true
    case Command.SetInfo(":status", "unsat") => false
  }

  /** The expected status of each `check-sat`, from the last `set-info :status`
   *  before it (the incremental convention; a single status covers them all).
   */
  def expectedStatuses: List[Option[Boolean]] =
    var status: Option[Boolean] = None
    var seen = false
    val out = List.newBuilder[Option[Boolean]]
    for c <- commands do
      c match
        case Command.SetInfo(":status", "sat") => status = Some(true); seen = true
        case Command.SetInfo(":status", "unsat") => status = Some(false); seen = true
        case Command.SetInfo(":status", _) => status = None; seen = true
        case Command.CheckSat | Command.CheckSatAssuming(_) =>
          out += status
          if seen then status = None
        case _ => ()
    out.result()

case class Smt2Error(message: String) extends Unsupported(message)
