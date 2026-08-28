package net.bovel.cdclt

/** A propositional variable: `0 until numVars`. */
type Var = Int

/** A literal: `2 * v` for the variable `v`, `2 * v + 1` for its negation. */
type Lit = Int

object Lit:
  def apply(v: Var, positive: Boolean = true): Lit = 2 * v + (if positive then 0 else 1)
  def variable(lit: Lit): Var = lit >> 1
  def positive(lit: Lit): Boolean = (lit & 1) == 0
  def neg(lit: Lit): Lit = lit ^ 1
  def show(lit: Lit): String = (if positive(lit) then "" else "-") + variable(lit)
