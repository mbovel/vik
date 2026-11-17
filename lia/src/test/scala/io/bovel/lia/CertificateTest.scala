package io.bovel.lia

import scala.collection.mutable.HashMap
import scala.util.Random

import Solver.Result

class CertificateTest extends munit.FunSuite:
  /** A random system of bounds on random combinations of `n` integer variables
   *  (and `reals` real ones), asserted with proofs on, then checked: every
   *  `Unsat` has a certificate that derives a contradiction from the bounds of
   *  its reasons. Returns whether it was `Unsat`, and the `Trusted` steps met.
   */
  def run(rnd: Random, n: Int, reals: Int, m: Int, range: Int): (Boolean, Int) =
    val solver = Solver()
    solver.proofs = true
    val vars = IndexedSeq.fill(n)(solver.newVar()) ++ IndexedSeq.fill(reals)(solver.newVar(integer = false))
    val asserted = HashMap.empty[Int, Certificate.Hyp]
    var result: Result = Result.Sat
    var k = 0
    while k < m && result == Result.Sat do
      val terms =
        List.fill(1 + rnd.nextInt(3))((BigInt(rnd.nextInt(2 * range + 1) - range), vars(rnd.nextInt(vars.length))))
          .filter(_._1 != 0)
          .distinctBy(_._2)
      if terms.nonEmpty then
        val x = terms match
          case (a, x) :: Nil if a == 1 => x
          case _ => solver.newSlack(terms, integer = terms.forall((_, y) => solver.isInteger(y)))
        val c = BigInt(rnd.nextInt(4 * range + 1) - 2 * range)
        val upper = rnd.nextBoolean()
        val strict = rnd.nextInt(4) == 0
        asserted(k) = Certificate.Hyp(k, x, c, strict, upper)
        result = if upper then solver.assertUpper(x, c, strict, k) else solver.assertLower(x, c, strict, k)
      k += 1
    if result == Result.Sat then result = solver.check()
    result match
      case Result.Unsat(reasons) =>
        val checker = Certificate.Checker(
          solver.form,
          solver.isInteger,
          h => if asserted.get(h.reason).contains(h) then None else Some(s"not asserted: $h")
        )
        assertEquals(checker.check(solver.conflictCertificate.nn, reasons), None)
        (true, checker.trusted)
      case _ => (false, 0)

  test("random integer systems: every unsat is certified"):
    val rnd = Random(1)
    var unsats = 0
    var trusted = 0
    for _ <- 0 until 2000 do
      val (unsat, t) = run(rnd, 2 + rnd.nextInt(4), 0, 3 + rnd.nextInt(8), 1 + rnd.nextInt(5))
      if unsat then unsats += 1
      trusted += t
    assert(unsats > 300, s"$unsats unsats")
    assert(trusted < unsats / 10, s"$trusted trusted steps over $unsats unsats")

  test("random mixed systems: every unsat is certified"):
    val rnd = Random(2)
    var unsats = 0
    for _ <- 0 until 2000 do
      if run(rnd, 1 + rnd.nextInt(3), 1 + rnd.nextInt(2), 3 + rnd.nextInt(8), 1 + rnd.nextInt(5))._1 then unsats += 1
    assert(unsats > 300, s"$unsats unsats")

  test("a certificate that does not refute is refused"):
    val solver = Solver()
    solver.proofs = true
    val x = solver.newVar()
    val checker = Certificate.Checker(solver.form, solver.isInteger, _ => None)
    // x ≤ 1 and x ≥ 1 are consistent.
    val c = Certificate.Comb(
      Array(Rational.one, Rational.one),
      Array(Certificate.Hyp(0, x, 1, false, true), Certificate.Hyp(1, x, 1, false, false))
    )
    assert(checker.check(c, Set(0, 1)).isDefined)
    // x < 1 and x ≥ 1 are not, once rounded or not.
    val d = Certificate.Comb(
      Array(Rational.one, Rational.one),
      Array(Certificate.Hyp(0, x, 1, true, true), Certificate.Hyp(1, x, 1, false, false))
    )
    assertEquals(checker.check(d, Set(0, 1)), None)
    // A hypothesis the lemma does not have.
    assert(checker.check(d, Set(0)).isDefined)
