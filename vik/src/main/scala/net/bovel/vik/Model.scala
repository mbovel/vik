package net.bovel.vik

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
      Term.Num(terms.map((a, t) => a * number(value(t))).sum + c)
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
    case Formula.Le(l, r) => number(value(l)) <= number(value(r))
    case Formula.Pred(t) =>
      value(t) match
        case Term.Bool(Formula.True) => true
        case Term.Bool(Formula.False) => false
        case other => other == Term.Bool(Formula.True)

  private def number(t: Term): BigInt = t match
    case Term.Num(v) => v
    case _ => 0 // an unconstrained integer: the model chose 0

object Model:
  /** A declared function in a model: its parameter sorts, result sort, the rows
   *  of its graph (argument values to result value) and the value everywhere
   *  else.
   */
  final case class Function(parameters: List[String], result: String, rows: List[(List[Term], Term)], default: Term)
