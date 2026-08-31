package net.bovel.vik

import scala.collection.mutable.HashMap

import Formula.*
import Term.*

/** Simplifications of a set of assertions before encoding.
 *
 *  Local constant elimination: in a conjunction `c = t ∧ φ` in a positive
 *  position (under `And`, `Or`, the branches of `Ite` and the right side of
 *  `Implies` only: under a negation the conjunction is not an existential
 *  context) where every occurrence of the constant `c` in the whole assertion
 *  set lies inside that conjunction and `t` does not mention `c`, `c` is
 *  existentially local, so the conjunction is equivalent to `φ[t/c]`. This
 *  turns the diamonds of the `eq_diamond` benchmarks,
 *  `(x = y ∧ y = x') ∨ (x = z ∧ z = x')` with `y` and `z` used nowhere else,
 *  into `x = x'`, which the solver then propagates at level 0 instead of
 *  enumerating the paths (z3 and cvc5 preprocess likewise).
 */
object Simplifier:
  /** `interpreted` names constants with a meaning of their own (nullary
   *  constructors of datatypes): they are never eliminated. Returns the
   *  simplified assertions and the constants eliminated by substitution — an
   *  elimination is sound only while no other assertion mentions the constant,
   *  so a caller that keeps the result and later learns of one must start over.
   */
  def simplify(assertions: List[Formula], interpreted: Set[String] = Set.empty): (List[Formula], Set[String]) =
    val total = HashMap.empty[String, Int]
    for f <- assertions do countConstants(f, total)
    for name <- interpreted do total(name) = Int.MaxValue // never local to a conjunction
    val out = assertions.map(f => simplify(f, total, true)).filter(_ != True)
    val left = HashMap.empty[String, Int]
    for f <- out do countConstants(f, left)
    (out, total.keySet.filterNot(left.contains).toSet -- interpreted)

  private def countConstants(f: Formula, counts: HashMap[String, Int]): Unit = f match
    case True | False => ()
    case Not(a) => countConstants(a, counts)
    case And(as) => as.foreach(countConstants(_, counts))
    case Or(as) => as.foreach(countConstants(_, counts))
    case Implies(a, b) =>
      countConstants(a, counts)
      countConstants(b, counts)
    case Iff(a, b) =>
      countConstants(a, counts)
      countConstants(b, counts)
    case Ite(c, t, e) =>
      countConstants(c, counts)
      countConstants(t, counts)
      countConstants(e, counts)
    case Eq(l, r) =>
      countConstants(l, counts)
      countConstants(r, counts)
    case Pred(t) => countConstants(t, counts)
    case Le(l, r) =>
      countConstants(l, counts)
      countConstants(r, counts)

  private def countConstants(t: Term, counts: HashMap[String, Int]): Unit = t match
    case Const(name) => counts(name) = counts.getOrElse(name, 0) + 1
    case App(fun, args) =>
      countConstants(fun, counts)
      args.foreach(countConstants(_, counts))
    case Num(_) | Var(_) => ()
    case Linear(terms, _) => terms.foreach((_, u) => countConstants(u, counts))
    case Lambda(_, body) => countConstants(body, counts)
    case Bool(formula) => countConstants(formula, counts)

  /** `positive`: whether `f` is in a positive position of the assertion. */
  private def simplify(f: Formula, total: HashMap[String, Int], positive: Boolean): Formula = f match
    case True | False | Eq(_, _) | Pred(_) | Le(_, _) => f
    case Not(a) => Not(simplify(a, total, false))
    case Or(as) => Or(as.map(simplify(_, total, positive)))
    case Implies(a, b) => Implies(simplify(a, total, false), simplify(b, total, positive))
    case Iff(a, b) => Iff(simplify(a, total, false), simplify(b, total, false))
    case Ite(c, t, e) => Ite(simplify(c, total, false), simplify(t, total, positive), simplify(e, total, positive))
    case And(as) if !positive => And(as.map(simplify(_, total, false)))
    case And(as) =>
      var conjuncts = as.map(simplify(_, total, true))
      // Eliminate the constants local to this conjunction, one equality at a time.
      var changed = true
      while changed do
        changed = false
        val local = HashMap.empty[String, Int]
        conjuncts.foreach(countConstants(_, local))
        conjuncts.zipWithIndex.collectFirst {
          case (Eq(Const(c), t), i) if local.getOrElse(c, 0) == total(c) && !mentions(t, c) => (c, t, i)
          case (Eq(t, Const(c)), i) if local.getOrElse(c, 0) == total(c) && !mentions(t, c) => (c, t, i)
        } match
          case Some((c, t, i)) =>
            conjuncts = conjuncts.zipWithIndex.collect { case (g, j) if j != i => substitute(g, c, t) }
            changed = true
          case None => ()
      conjuncts match
        case Nil => True
        case single :: Nil => single
        case _ => And(conjuncts)

  private def mentions(t: Term, c: String): Boolean = t match
    case Const(name) => name == c
    case App(fun, args) => mentions(fun, c) || args.exists(mentions(_, c))
    case Num(_) | Var(_) => false
    case Linear(terms, _) => terms.exists((_, u) => mentions(u, c))
    case Lambda(_, body) => mentions(body, c)
    case Bool(formula) => formula.terms.exists(mentions(_, c))

  private def substitute(f: Formula, c: String, t: Term): Formula = f match
    case True | False => f
    case Not(a) => Not(substitute(a, c, t))
    case And(as) => And(as.map(substitute(_, c, t)))
    case Or(as) => Or(as.map(substitute(_, c, t)))
    case Implies(a, b) => Implies(substitute(a, c, t), substitute(b, c, t))
    case Iff(a, b) => Iff(substitute(a, c, t), substitute(b, c, t))
    case Ite(a, b, d) => Ite(substitute(a, c, t), substitute(b, c, t), substitute(d, c, t))
    case Eq(l, r) => Eq(substitute(l, c, t), substitute(r, c, t))
    case Pred(u) => Formula.pred(substitute(u, c, t))
    case Le(l, r) => Le(substitute(l, c, t), substitute(r, c, t))

  private def substitute(u: Term, c: String, t: Term): Term = u match
    case Const(name) => if name == c then t else u
    case App(fun, args) => Term.app(substitute(fun, c, t), args.map(substitute(_, c, t))) // reduced if `t` is a lambda
    case Num(_) | Var(_) => u
    case Linear(terms, k) => Term.linear(terms.map((a, v) => (a, substitute(v, c, t))), k)
    case Lambda(sorts, body) => Lambda(sorts, substitute(body, c, t)) // `t` is closed: no capture
    case Bool(formula) => Term.boolean(substitute(formula, c, t))
