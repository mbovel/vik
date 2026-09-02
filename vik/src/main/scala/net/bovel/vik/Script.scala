package net.bovel.vik

import net.bovel.sexpr.SExpr
import net.bovel.smtlib.Datatype

/** A command of a lowered script: the player's instruction set, over the
 *  solver's [[Term]]s and [[Formula]]s. [[Lowering]] produces these from the
 *  surface commands of `net.bovel.smtlib`.
 */
enum Command:
  case SetLogic(logic: String)
  case SetInfo(key: String, value: String)
  case DeclareSort(name: String, arity: Int)
  case DeclareFun(name: String, argSorts: List[String], sort: String)
  case DeclareDatatypes(datatypes: List[Datatype])
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

  /** `get-value`: each item as written (for the echo) and as lowered. */
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

  /** A command with no effect on solving (`echo`, ...). */
  case Other(sexpr: SExpr)

final case class Script(commands: List[Command])
