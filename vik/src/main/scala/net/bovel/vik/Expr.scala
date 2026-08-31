package net.bovel.vik

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

  /** A function of parameters of the given sorts, `Var(i)` in `body` being the
   *  `i`-th parameter of the innermost enclosing lambda, and `Var(n + i)` the
   *  `i`-th one of the lambda around it, `n` its number of parameters (de
   *  Bruijn indices: renaming a parameter changes nothing). See
   *  [[Term.instantiate]] for what the solver does with them.
   */
  case Lambda(sorts: List[String], body: Term)
  case Var(index: Int)

  /** A formula where a term is expected (an argument, a lambda body, a field):
   *  a `Bool`-sorted term. Made by [[Term.boolean]], which keeps a term that is
   *  already one (`Pred(t)` is `t`). The encoder gives it a class and ties its
   *  truth to the formula's.
   */
  case Bool(formula: Formula)

  override def toString: String = this match
    case Const(name) => name
    case App(fun, args) => s"($fun ${args.mkString(" ")})"
    case Num(value) => value.toString
    case Lambda(sorts, body) => s"(λ${sorts.mkString(" ")}. $body)"
    case Var(index) => s"_$index"
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
  /** Lambdas in the solver (after the e-graph of dotty's qualified types). A
   *  lambda is a node of the e-graph over the class of its body, and a bound
   *  variable `Var(i)` a leaf class shared by every lambda: two lambdas are
   *  equal when their bodies are, alpha-equivalence included, by congruence.
   *  Beta-reduction of a redex `(λ. b)(args)` with closed arguments happens at
   *  construction ([[app]]): in the parser, the simplifier's substitutions and
   *  [[instantiate]] itself, so that terms are in normal form (they have one
   *  when well sorted). A redex the search creates, `f(args)` with `f` equal to
   *  a lambda `l`, is a lemma of the encoder: `f ≠ l ∨ f(args) = l(args)` with
   *  `l(args)` the body instantiated (no capture, no index shifting).
   *
   *  Soundness: an equality between open terms in the e-graph holds for all
   *  values of their free variables, by induction. Nothing asserts one: the
   *  encoder makes atoms on closed terms only (assertions, the lemmas, the
   *  sharing of integers and the exhaustiveness of datatypes skip open terms),
   *  and no rule concludes from the equality of two lambdas to that of their
   *  bodies. And every rule preserves it: congruence, the datatype rules (`C(a) =
   *  C(b)` gives `a = b`, `sel(C(a)) = a`, ... for all `a`, `b`) and the lemmas
   *  (valid for functions). Extensionality — `f ≠ g` gives some `c` with
   *  `f(c) ≠ g(c)` — is a lemma of the encoder for the equalities of the
   *  assertions, with fresh witnesses (`Encoder.assertExtensionality`).
   */
  /** `fun` applied to `args`, in canonical form: an application of an
   *  application is one application (`(f a) b` is `f a b`, as in SMT-LIB), and
   *  a lambda applied to closed arguments is reduced — to fewer arguments than
   *  parameters, to a lambda of the remaining ones.
   */
  def app(fun: Term, args: List[Term]): Term =
    if args.isEmpty then fun
    else
      fun match
        case App(f, as) => app(f, as ++ args)
        case lambda: Lambda if args.forall(closed(_)) =>
          val n = lambda.sorts.length
          if args.length <= n then instantiate(lambda, args)
          else app(instantiate(lambda, args.take(n)), args.drop(n))
        case _ => App(fun, args)

  /** A formula as a term: `Pred(t)` is `t` itself. */
  def boolean(formula: Formula): Term = formula match
    case Formula.Pred(t) => t
    case _ => Bool(formula)

  /** The body of `lambda` with its first parameters replaced by `args`
   *  (closed): the whole body when there are as many arguments as parameters, a
   *  lambda of the remaining parameters otherwise.
   */
  def instantiate(lambda: Lambda, args: List[Term]): Term =
    val params = args.length
    require(params <= lambda.sorts.length && args.forall(closed(_)), "closed arguments, at most as many as parameters")
    def subst(t: Term, depth: Int): Term = t match
      case Var(i) if i >= depth && i < depth + params => args(i - depth)
      case Var(i) if i >= depth + params => Var(i - params)
      case Var(_) | Const(_) | Num(_) => t
      case App(f, as) => app(subst(f, depth), as.map(subst(_, depth)))
      case Linear(terms, c) => Term.linear(terms.map((a, u) => (a, subst(u, depth))), c)
      case Lambda(sorts, body) => Lambda(sorts, subst(body, depth + sorts.length))
      case Bool(formula) => boolean(formula.mapTerms(subst(_, depth)))
    val body = subst(lambda.body, 0) // the remaining parameters become `Var(0)`, `Var(1)`, ...
    val rest = lambda.sorts.drop(params)
    if rest.isEmpty then body else Lambda(rest, body)

  /** `t` with the indices of its free variables raised by `k`: `t` moved under
   *  `k` more binders.
   */
  def shift(t: Term, k: Int, depth: Int = 0): Term =
    if k == 0 then t
    else
      t match
        case Var(i) if i >= depth => Var(i + k)
        case Var(_) | Const(_) | Num(_) => t
        case App(f, as) => App(shift(f, k, depth), as.map(shift(_, k, depth)))
        case Linear(terms, c) => Term.linear(terms.map((a, u) => (a, shift(u, k, depth))), c)
        case Lambda(sorts, body) => Lambda(sorts, shift(body, k, depth + sorts.length))
        case Bool(formula) => boolean(formula.mapTerms(shift(_, k, depth)))

  /** Whether `t` mentions no bound variable of an enclosing lambda. */
  /** The names of the constants of a term, `fun` positions included. */
  def constants(term: Term): Set[String] =
    val out = scala.collection.mutable.HashSet.empty[String]
    def walk(t: Term): Unit = t match
      case Const(name) => out += name
      case App(fun, args) =>
        walk(fun)
        args.foreach(walk)
      case Num(_) | Var(_) => ()
      case Linear(terms, _) => terms.foreach((_, u) => walk(u))
      case Lambda(_, body) => walk(body)
      case Bool(formula) => formula.terms.foreach(walk)
    walk(term)
    out.toSet

  def closed(t: Term, depth: Int = 0): Boolean = t match
    case Var(i) => i < depth
    case Const(_) | Num(_) => true
    case App(f, as) => closed(f, depth) && as.forall(closed(_, depth))
    case Linear(terms, _) => terms.forall((_, u) => closed(u, depth))
    case Lambda(sorts, body) => closed(body, depth + sorts.length)
    case Bool(formula) => formula.terms.forall(closed(_, depth))

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
