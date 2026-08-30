package net.bovel.smt

/** Where a lowering puts what it needs: the fresh constants of an encoding
 *  (`bv`, the overflow counts, the quotients and remainders of a division) and
 *  the assertions that tie them to the term they stand for. The front end is
 *  one, declaring the constants and asserting the definitions before the
 *  assertion being read (see [[BitVectors]]).
 */
trait Lowering:
  /** A fresh `Int` constant in `[lo, hi]`, declared and bounded. */
  def freshInt(prefix: String, lo: BigInt, hi: BigInt): Term

  /** Asserts `formula`, which defines the constants above. */
  def define(formula: Formula): Unit
