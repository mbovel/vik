package net.bovel.smt

import scala.util.hashing.MurmurHash3

/** A term of an uninterpreted sort: a constant, or the application of a term (a
 *  function symbol, itself a constant) to arguments, as in the e-graph.
 */
enum Term:
  case Const(name: String)
  case App(fun: Term, args: List[Term])

  override def toString: String = this match
    case Const(name) => name
    case App(fun, args) => s"($fun ${args.mkString(" ")})"

  /** The structural hash, computed once: the encoder looks terms up in hash
   *  maps, and a subterm shared through `let` would otherwise be hashed in full
   *  at each lookup of each term containing it.
   */
  private lazy val hash: Int = MurmurHash3.productHash(this)
  override def hashCode: Int = hash

/** A quantifier-free formula over equalities of terms and Boolean-sorted terms
 *  (`Pred(t)` holds when `t` is true), with the usual connectives.
 */
enum Formula:
  case True
  case False
  case Not(arg: Formula)
  case And(args: List[Formula])
  case Or(args: List[Formula])
  case Implies(lhs: Formula, rhs: Formula)
  case Iff(lhs: Formula, rhs: Formula)
  case Ite(cond: Formula, thn: Formula, els: Formula)
  case Eq(lhs: Term, rhs: Term)
  case Pred(term: Term)

  /** Computed once, as for terms. */
  private lazy val hash: Int = MurmurHash3.productHash(this)
  override def hashCode: Int = hash

  def unary_! : Formula = Not(this)
  def &&(that: Formula): Formula = And(List(this, that))
  def ||(that: Formula): Formula = Or(List(this, that))
  def ==>(that: Formula): Formula = Implies(this, that)
  def <=>(that: Formula): Formula = Iff(this, that)

object Formula:
  def Neq(lhs: Term, rhs: Term): Formula = Not(Eq(lhs, rhs))

  /** `And` of the pairwise disequalities. */
  def Distinct(terms: List[Term]): Formula =
    And(for (a, i) <- terms.zipWithIndex; b <- terms.drop(i + 1) yield Neq(a, b))

extension (lhs: Term)
  def ===(rhs: Term): Formula = Formula.Eq(lhs, rhs)
  def !==(rhs: Term): Formula = Formula.Neq(lhs, rhs)
