package net.bovel.smt2

/** An algebraic datatype declaration: constructors with their selectors (name
 *  and sort). A constructor is applied as a function symbol (`App(Const(name),
 *  args)`), a selector as a unary function, and the tester of a constructor `C`
 *  is the unary function `(_ is C)`. The solver decides them by rules of its
 *  e-graph — injectivity, distinctness, selectors and testers on merges — with
 *  exhaustiveness and acyclicity as lemmas (the solver's `AdtRules`).
 */
final case class Datatype(name: String, constructors: List[Constructor])

final case class Constructor(name: String, selectors: List[(String, Sort)]):
  def tester: String = s"(_ is $name)"
