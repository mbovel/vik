package net.bovel.vik

import scala.util.hashing.MurmurHash3

/** The solver's term language — what [[Lowering]] produces and everything
 *  downstream (the simplifier, the encoder, the theories, the models) speaks.
 *  The surface language of SMT-LIB lives in `net.bovel.smtlib`; a surface tree
 *  cannot reach the solver by type. A term is: of an uninterpreted sort, a
 *  constant or the application of a term (a function symbol, itself a constant)
 *  to arguments, as in the e-graph; of sort `Int`, also a numeral or a linear
 *  combination `Σ coefficient × term + constant` of `Int`-sorted constants (the
 *  operators `+`, `-` and `*` by a numeral, in the normal form of
 *  [[Term.linear]]).
 */
enum Term:
  case Const(name: String)
  case App(fun: Term, args: List[Term])
  case Num(value: BigInt)
  case Linear(terms: List[(BigInt, Term)], constant: BigInt)

  /** A formula where a term is expected (an argument, a field): a `Bool`-sorted
   *  term. Made by [[Term.boolean]], which keeps a term that is already one
   *  (`Pred(t)` is `t`). The encoder gives it a class and ties its truth to the
   *  formula's.
   */
  case Bool(formula: Formula)

  override def toString: String = this match
    case Const(name) => name
    case App(fun, args) => s"($fun ${args.mkString(" ")})"
    case Num(value) => value.toString
    case Bool(formula) => formula.toString
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
  /** A formula as a term: `Pred(t)` is `t` itself. */
  def boolean(formula: Formula): Term = formula match
    case Formula.Pred(t) => t
    case _ => Bool(formula)

  /** The names of the constants of a term, `fun` positions included. */
  def constants(term: Term): Set[String] =
    val out = scala.collection.mutable.HashSet.empty[String]
    def walk(t: Term): Unit = t match
      case Const(name) => out += name
      case App(fun, args) =>
        walk(fun)
        args.foreach(walk)
      case Num(_) => ()
      case Linear(terms, _) => terms.foreach((_, u) => walk(u))
      case Bool(formula) => formula.terms.foreach(walk)
    walk(term)
    out.toSet

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

  /** `lhs < rhs`, strictly: the atom of the reals — an integer strict
   *  comparison becomes [[Formula.Lt]]'s inclusive rewrite instead.
   */
  case Less(lhs: Term, rhs: Term)

  /** Computed once, as for terms. */
  private lazy val hash: Int = MurmurHash3.productHash(this)
  override def hashCode: Int = hash

  /** The terms at the leaves: the sides of `Eq` and `Le`, the term of `Pred`.
   */
  def terms: List[Term] = this match
    case True | False => Nil
    case Not(arg) => arg.terms
    case And(args) => args.flatMap(_.terms)
    case Or(args) => args.flatMap(_.terms)
    case Implies(lhs, rhs) => lhs.terms ++ rhs.terms
    case Iff(lhs, rhs) => lhs.terms ++ rhs.terms
    case Ite(cond, thn, els) => cond.terms ++ thn.terms ++ els.terms
    case Eq(lhs, rhs) => List(lhs, rhs)
    case Pred(term) => List(term)
    case Le(lhs, rhs) => List(lhs, rhs)
    case Less(lhs, rhs) => List(lhs, rhs)

  /** The formula with `f` applied to the terms at the leaves (a `Pred` of a
   *  Boolean term becomes its formula).
   */
  def mapTerms(f: Term => Term): Formula = this match
    case True | False => this
    case Not(arg) => Not(arg.mapTerms(f))
    case And(args) => And(args.map(_.mapTerms(f)))
    case Or(args) => Or(args.map(_.mapTerms(f)))
    case Implies(lhs, rhs) => Implies(lhs.mapTerms(f), rhs.mapTerms(f))
    case Iff(lhs, rhs) => Iff(lhs.mapTerms(f), rhs.mapTerms(f))
    case Ite(cond, thn, els) => Ite(cond.mapTerms(f), thn.mapTerms(f), els.mapTerms(f))
    case Eq(lhs, rhs) => Eq(f(lhs), f(rhs))
    case Pred(term) => Formula.pred(f(term))
    case Le(lhs, rhs) => Le(f(lhs), f(rhs))
    case Less(lhs, rhs) => Less(f(lhs), f(rhs))

  def unary_! : Formula = Not(this)
  def &&(that: Formula): Formula = And(List(this, that))
  def ||(that: Formula): Formula = Or(List(this, that))
  def ==>(that: Formula): Formula = Implies(this, that)
  def <=>(that: Formula): Formula = Iff(this, that)

object Formula:
  def Neq(lhs: Term, rhs: Term): Formula = Not(Eq(lhs, rhs))

  /** `term` holds: the formula of a Boolean term, `Pred(term)` otherwise. */
  def pred(term: Term): Formula = term match
    case Term.Bool(formula) => formula
    case _ => Pred(term)

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
