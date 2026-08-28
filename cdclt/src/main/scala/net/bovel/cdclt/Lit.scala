package net.bovel.cdclt

import scala.reflect.ClassTag

/** A propositional variable: `0 until numVars`. */
type Var = Int

/** A literal: a variable or its negation, encoded as `2 * v` or `2 * v + 1`. */
opaque type Lit = Int

object Lit:
  def apply(v: Var, positive: Boolean = true): Lit = 2 * v + (if positive then 0 else 1)

  /** The literal at `index` (see `index`). */
  def fromIndex(index: Int): Lit = index

  /** Arrays of literals are arrays of `Int`. */
  given ClassTag[Lit] = summon[ClassTag[Int]]

  extension (lit: Lit)
    def variable: Var = lit >> 1
    def positive: Boolean = (lit & 1) == 0
    def neg: Lit = lit ^ 1

    /** The position of `lit` in arrays indexed by literals:
     *  `0 until 2 * numVars`.
     */
    def index: Int = lit
    def show: String = (if lit.positive then "" else "-") + lit.variable
