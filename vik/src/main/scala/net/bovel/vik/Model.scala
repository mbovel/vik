package net.bovel.vik

import net.bovel.lia

/** A model of a `Sat` answer, taken from the engines before the solver unwound:
 *  values for the declared constants, and a finite graph per declared function
 *  — the applications the problem mentions, and a default.
 *
 *  Values are closed terms: a numeral, `true`/`false` as a [[Term.Bool]], a
 *  constructor application for a datatype, or an abstract element `@Sort!k` for
 *  an uninterpreted sort — distinct constants map to distinct elements exactly
 *  when the e-graph kept them apart.
 */
final case class Model(
    constants: Map[String, Term],
    functions: Map[String, Model.Function]
):
  /** The value of a term under this model: arithmetic is computed, a function
   *  application is looked up in its graph (the default when absent), and an
   *  unknown constant of an uninterpreted sort is itself.
   */
  def value(term: Term): Term = term match
    case Term.Num(_) => term
    case Term.Linear(terms, c) =>
      Model.rationalTerm(terms.foldLeft(lia.Rational(c)) { case (sum, (a, t)) =>
        sum + lia.Rational(a) * rational(value(t))
      })
    case Term.Const(name) =>
      functions.get(name) match
        case Some(f) if f.parameters.isEmpty => f.default
        case _ => constants.getOrElse(name, term)
    case Term.App(Term.Const(name), args) =>
      val values = args.map(value)
      functions.get(name) match
        case Some(f) => f.rows.collectFirst { case (a, r) if a == values => r }.getOrElse(f.default)
        case None => term
    case Term.Bool(formula) => Term.boolean(if holds(formula) then Formula.True else Formula.False)
    case _ => term

  /** Whether a formula holds under this model. */
  def holds(formula: Formula): Boolean = formula match
    case Formula.True => true
    case Formula.False => false
    case Formula.Not(a) => !holds(a)
    case Formula.And(as) => as.forall(holds)
    case Formula.Or(as) => as.exists(holds)
    case Formula.Implies(a, b) => !holds(a) || holds(b)
    case Formula.Iff(a, b) => holds(a) == holds(b)
    case Formula.Ite(c, t, e) => if holds(c) then holds(t) else holds(e)
    case Formula.Eq(l, r) => value(l) == value(r)
    case Formula.Le(l, r) => rational(value(l)) <= rational(value(r))
    case Formula.Less(l, r) => rational(value(l)) < rational(value(r))
    case Formula.Pred(t) => value(t) == Term.Bool(Formula.True)

  private def rational(t: Term): lia.Rational = t match
    case Term.Num(v) => lia.Rational(v)
    case Term.App(Term.Const("/"), List(p, q)) => rational(p) / rational(q)
    case _ => lia.Rational(BigInt(0)) // an unconstrained number: the model chose 0

object Model:
  /** A rational as a term: a numeral, or `(/ num den)`. */
  def rationalTerm(r: lia.Rational): Term =
    if r.isInteger then Term.Num(r.num)
    else Term.App(Term.Const("/"), List(Term.Num(r.num), Term.Num(r.den)))

  /** A declared function in a model: its parameter sorts, result sort, the rows
   *  of its graph (argument values to result value) and the value everywhere
   *  else.
   */
  final case class Function(parameters: List[String], result: String, rows: List[(List[Term], Term)], default: Term)
