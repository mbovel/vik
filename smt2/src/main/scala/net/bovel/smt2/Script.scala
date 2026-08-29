package net.bovel.smt2

import net.bovel.sexpr.SExpr
import net.bovel.smt.{Datatype, Formula}

/** A command of an SMT-LIB 2 script, restricted to what the supported logics
 *  need.
 */
enum Command:
  case SetLogic(logic: String)
  case SetInfo(key: String, value: String)
  case DeclareSort(name: String, arity: Int)
  case DeclareFun(name: String, argSorts: List[String], sort: String)
  case DeclareDatatypes(datatypes: List[Datatype])
  case DefineFun(name: String, params: List[(String, String)], sort: String, body: SExpr)
  case Assert(formula: Formula)
  case CheckSat
  case Push(levels: Int)
  case Pop(levels: Int)
  case GetModel
  case Exit

  /** A command with no effect on solving (`set-option`, `get-info`, `echo`,
   *  ...).
   */
  case Other(sexpr: SExpr)

final case class Script(commands: List[Command]):
  /** The `:status` from `set-info`, if any: `Some(true)` for sat. */
  def expectedStatus: Option[Boolean] = commands.collectFirst {
    case Command.SetInfo(":status", "sat") => true
    case Command.SetInfo(":status", "unsat") => false
  }

case class Smt2Error(message: String) extends Exception(message)
