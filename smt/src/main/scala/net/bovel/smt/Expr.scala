package net.bovel.smt

import scala.util.hashing.MurmurHash3

/** A term: of an uninterpreted sort, a constant or the application of a term (a
 *  function symbol, itself a constant) to arguments, as in the e-graph; of sort
 *  `Int`, also a numeral or a linear combination `Σ coefficient × term +
 *  constant` of `Int`-sorted constants (the `Ints` operators `+`, `-` and `*`
 *  by a numeral, in the normal form of [[Term.linear]]).
 */
enum Term:
  case Const(name: String)
  case App(fun: Term, args: List[Term])
  case Num(value: BigInt)
  case Linear(terms: List[(BigInt, Term)], constant: BigInt)

  override def toString: String = this match
    case Const(name) => name
    case App(fun, args) => s"($fun ${args.mkString(" ")})"
    case Num(value) => value.toString
    case Linear(terms, constant) =>
      terms.map((a, t) => if a == 1 then s"$t" else s"${a}·$t").mkString(" + ") +
        (if constant.signum == 0 then "" else s" + $constant")

  /** The structural hash, computed once: the encoder looks terms up in hash
   *  maps, and a subterm shared through `let` would otherwise be hashed in full
   *  at each lookup of each term containing it.
   */
  private lazy val hash: Int = MurmurHash3.productHash(this)
  override def hashCode: Int = hash

object Term:
  /** The linear combination `Σ a × t + c`, normalized: nested combinations and
   *  numerals folded in, equal terms merged, zero coefficients dropped; a
   *  numeral, or a lone term, when that is all that remains.
   */
  def linear(terms: List[(BigInt, Term)], constant: BigInt = 0): Term =
    val coefficients = scala.collection.mutable.LinkedHashMap.empty[Term, BigInt]
    var c = constant
    def add(a: BigInt, t: Term): Unit = t match
      case Num(v) => c += a * v
      case Linear(ts, k) =>
        c += a * k
        for (b, u) <- ts do add(a * b, u)
      case _ => coefficients(t) = coefficients.getOrElse(t, BigInt(0)) + a
    for (a, t) <- terms do add(a, t)
    coefficients.toList.collect { case (t, a) if a.signum != 0 => (a, t) } match
      case Nil => Num(c)
      case (a, t) :: Nil if a == 1 && c.signum == 0 => t
      case ts => Linear(ts, c)

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

  /** `lhs ≤ rhs` on `Int`-sorted terms: `<`, `≥`, `>`, `=` and `distinct` on
   *  integers reduce to it (see the companion).
   */
  case Le(lhs: Term, rhs: Term)

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

  /** `lhs < rhs` on integers: `lhs + 1 ≤ rhs`. */
  def Lt(lhs: Term, rhs: Term): Formula = Le(Term.linear(List((1, lhs)), 1), rhs)
  def Ge(lhs: Term, rhs: Term): Formula = Le(rhs, lhs)
  def Gt(lhs: Term, rhs: Term): Formula = Lt(rhs, lhs)

  /** `lhs = rhs` on integers: two bounds. */
  def IntEq(lhs: Term, rhs: Term): Formula = And(List(Le(lhs, rhs), Le(rhs, lhs)))

  /** `distinct` on integers: `And` of the pairwise `Or(Lt, Gt)`. */
  def IntDistinct(terms: List[Term]): Formula =
    And(for (a, i) <- terms.zipWithIndex; b <- terms.drop(i + 1) yield Or(List(Lt(a, b), Lt(b, a))))

extension (lhs: Term)
  def ===(rhs: Term): Formula = Formula.Eq(lhs, rhs)
  def !==(rhs: Term): Formula = Formula.Neq(lhs, rhs)
  def <=(rhs: Term): Formula = Formula.Le(lhs, rhs)
  def <(rhs: Term): Formula = Formula.Lt(lhs, rhs)
  def >=(rhs: Term): Formula = Formula.Ge(lhs, rhs)
  def >(rhs: Term): Formula = Formula.Gt(lhs, rhs)
  def +(rhs: Term): Term = Term.linear(List((1, lhs), (1, rhs)))
  def -(rhs: Term): Term = Term.linear(List((1, lhs), (-1, rhs)))
  def *(k: BigInt): Term = Term.linear(List((k, lhs)))
