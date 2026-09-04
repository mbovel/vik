package io.bovel.vik

import io.bovel.lia

/** A model of a `Sat` answer, taken from the engines before the solver unwound:
 *  values for the declared constants, and a finite graph per declared function
 *  — the applications the problem mentions, and a default.
 *
 *  Values are closed terms: a numeral, `true`/`false` as a [[Term.Bool]], a
 *  constructor application for a datatype, an abstract element `@Sort!k` for an
 *  uninterpreted sort — distinct constants map to distinct elements exactly
 *  when the e-graph kept them apart — or, for an array, the SMT-LIB form
 *  `(store (store ((as const (Array I E)) d) i v) j w)`: a default `d` and the
 *  indices the problem read or wrote ([[Model.select]], [[Model.store]]).
 */
final case class Model(
    constants: Map[String, Term],
    functions: Map[String, Model.Function],
    datatypes: List[io.bovel.smtlib.Datatype] = Nil
):
  /** A selector's constructor and position, a tester's constructor. */
  private lazy val selectors: Map[String, (String, Int)] =
    (for d <- datatypes; c <- d.constructors; ((s, _), i) <- c.selectors.zipWithIndex yield s -> (c.name, i)).toMap
  private lazy val testers: Map[String, String] =
    (for d <- datatypes; c <- d.constructors yield c.tester -> c.name).toMap

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
    case Term.App(Term.Const(name), List(a, i, v)) if name.startsWith("store!") =>
      Model.store(value(a), value(i), value(v))
    case Term.App(Term.Const(name), List(arg)) if selectors.contains(name) => // a selector on a constructor value
      val (constructor, i) = selectors(name)
      value(arg) match
        case Term.App(Term.Const(c), args) if c == constructor => args(i)
        case v => lookup(name, List(v)) // another constructor's value: unconstrained, the graph's choice
    case Term.App(Term.Const(name), List(arg)) if testers.contains(name) =>
      val holds = value(arg) match
        case Term.App(Term.Const(c), _) => c == testers(name)
        case Term.Const(c) => c == testers(name)
        case _ => false
      Term.boolean(if holds then Formula.True else Formula.False)
    case Term.App(fun, List(index)) if Model.isArray(value(fun)) => Model.select(value(fun), value(index))
    case Term.App(Term.Const(name), args) => lookup(name, args.map(value))
    case Term.Bool(formula) => Term.boolean(if holds(formula) then Formula.True else Formula.False)
    case _ => term

  /** A function application by the function's graph: the row of the argument
   *  values, else the default; the application itself for an unknown symbol.
   */
  private def lookup(name: String, values: List[Term]): Term = functions.get(name) match
    case Some(f) => f.rows.collectFirst { case (a, r) if a == values => r }.getOrElse(f.default)
    case None => Term.App(Term.Const(name), values)

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
    case Formula.Eq(l, r) => Model.same(value(l), value(r))
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

  /** The array of sort `sort` (as SMT-LIB writes it) holding `default`
   *  everywhere: `((as const (Array I E)) d)`.
   */
  def constArray(sort: String, default: Term): Term = Term.App(Term.Const(s"(as const $sort)"), List(default))

  /** `array` with `value` at `index`: `(store array index value)`. */
  def store(array: Term, index: Term, value: Term): Term = Term.App(Term.Const("store"), List(array, index, value))

  /** Whether a value is an array, in the form above. */
  def isArray(value: Term): Boolean = value match
    case Term.App(Term.Const("store"), List(_, _, _)) => true
    case Term.App(Term.Const(name), List(_)) => name.startsWith("(as const ")
    case _ => false

  /** The element of an array value at an index: the innermost write to it, else
   *  the default.
   */
  def select(array: Term, index: Term): Term = array match
    case Term.App(Term.Const("store"), List(a, i, v)) => if same(i, index) then v else select(a, index)
    case Term.App(Term.Const(name), List(d)) if name.startsWith("(as const ") => d
    case _ => Term.App(array, List(index))

  /** An array value as its default and the map of the indices written to a
   *  value other than it.
   */
  def graph(array: Term): (Term, Map[Term, Term]) = array match
    case Term.App(Term.Const("store"), List(a, i, v)) =>
      val (d, m) = graph(a)
      (d, if same(v, d) then m - i else m.updated(i, v))
    case Term.App(Term.Const(_), List(d)) => (d, Map.empty)
    case _ => (array, Map.empty)

  /** Whether two values are the same: arrays extensionally (their elements
   *  likewise, for arrays of arrays), the rest as terms.
   */
  def same(a: Term, b: Term): Boolean =
    if isArray(a) && isArray(b) then
      val ((da, ma), (db, mb)) = (graph(a), graph(b))
      same(da, db) && ma.keySet == mb.keySet && ma.forall((i, v) => same(v, mb(i)))
    else a == b
