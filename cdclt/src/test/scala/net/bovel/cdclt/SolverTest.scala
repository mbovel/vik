package net.bovel.cdclt

import scala.collection.mutable.ArrayBuffer

class SolverTest extends munit.FunSuite:

  def sat(numVars: Int, clauses: Seq[Seq[Lit]], theory: Theory = NoTheory): Option[Array[Boolean]] =
    val s = Solver(numVars, theory)
    for c <- clauses do s.addClause(c*)
    s.solve() match
      case Solver.Result.Sat(model) => Some(model)
      case Solver.Result.Unsat => None

  def satisfies(model: Array[Boolean], clauses: Seq[Seq[Lit]]): Boolean =
    clauses.forall(_.exists(l => model(l.variable) == l.positive))

  test("literals"):
    assertEquals(Lit(3).index, 6)
    assertEquals(Lit(3, false).index, 7)
    assertEquals(Lit(3).neg, Lit(3, false))
    assertEquals(Lit(3, false).variable, 3)
    assert(Lit(3).positive && !Lit(3, false).positive)

  test("empty problem is sat"):
    assert(sat(0, Nil).isDefined)

  test("a unit clause is propagated"):
    val model = sat(2, Seq(Seq(Lit(1)), Seq(Lit(0, false), Lit(1, false)))).get
    assert(model(1) && !model(0))

  test("x and not x is unsat"):
    assertEquals(sat(1, Seq(Seq(Lit(0)), Seq(Lit(0, false)))), None)

  test("the four clauses over two variables are unsat"):
    val x = Lit(0)
    val y = Lit(1)
    assertEquals(sat(2, Seq(Seq(x, y), Seq(x.neg, y), Seq(x, y.neg), Seq(x.neg, y.neg))), None)

  test("learning: a conflict below the first decision level backjumps"):
    // Decisions x0 = false, x1 = false, ... ; the clauses force a conflict involving x0 only after several decisions.
    val clauses = Seq(
      Seq(Lit(0), Lit(4)),
      Seq(Lit(0), Lit(4, false), Lit(5)),
      Seq(Lit(0), Lit(4, false), Lit(5, false))
    )
    val model = sat(6, clauses).get
    assert(satisfies(model, clauses) && model(0))

  /** Truth of `clauses` under every assignment of `numVars` variables. */
  def bruteForce(numVars: Int, clauses: Seq[Seq[Lit]], extra: Array[Boolean] => Boolean = _ => true): Boolean =
    (0 until (1 << numVars)).exists { bits =>
      val model = Array.tabulate(numVars)(v => (bits & (1 << v)) != 0)
      satisfies(model, clauses) && extra(model)
    }

  test("random 3-SAT agrees with brute force"):
    val rnd = scala.util.Random(3)
    var sats = 0
    for _ <- 0 until 300 do
      val n = 4 + rnd.nextInt(7)
      val m = (4.3 * n).toInt + rnd.nextInt(3)
      val clauses = Seq.fill(m)(Seq.fill(3)(Lit(rnd.nextInt(n), rnd.nextBoolean())))
      val result = sat(n, clauses)
      assertEquals(result.isDefined, bruteForce(n, clauses), s"n=$n clauses=$clauses")
      for model <- result do
        assert(satisfies(model, clauses))
        sats += 1
    assert(sats > 50)

  /** At most `k` true atoms in each group; conflicts and propagations are
   *  explained by the true atoms of the group.
   */
  class AtMostTheory(groups: Seq[(Seq[Var], Int)]) extends Theory:
    private val trueAtoms = ArrayBuffer.empty[Var]
    private val marks = ArrayBuffer.empty[Int]
    private val implied = collection.mutable.HashMap.empty[Var, Seq[Var]] // false atom -> the true atoms that imply it
    def assertLit(lit: Lit): Unit = if lit.positive then trueAtoms += lit.variable
    def propagate(): Theory.Result =
      val violated = groups.map((atoms, k) => (atoms.filter(trueAtoms.contains), k)).find((t, k) => t.length > k)
      if violated.isDefined then return Theory.Result.Conflict(ArrayBuffer.from(violated.get._1.map(Lit(_))))
      val out = ArrayBuffer.empty[Lit]
      for (atoms, k) <- groups do
        val t = atoms.filter(trueAtoms.contains)
        if t.length == k then
          for a <- atoms if !t.contains(a) do
            implied(a) = t
            out += Lit(a, false)
      Theory.Result.Ok(out)
    def explain(lit: Lit): ArrayBuffer[Lit] = ArrayBuffer.from(implied(lit.variable).map(Lit(_)))
    def push(): Unit = marks += trueAtoms.length
    def pop(): Unit = trueAtoms.takeInPlace(marks.remove(marks.length - 1))

  test("theory conflicts and propagations"):
    // At most one of x0, x1, x2 is true; the clauses ask for two of them.
    val theory = AtMostTheory(Seq((Seq(0, 1, 2), 1)))
    assertEquals(sat(3, Seq(Seq(Lit(0)), Seq(Lit(1), Lit(2))), theory), None)
    // With a third choice, the theory propagates the other two false and the search succeeds.
    val model = sat(4, Seq(Seq(Lit(0)), Seq(Lit(1), Lit(2), Lit(3))), AtMostTheory(Seq((Seq(0, 1, 2), 1)))).get
    assert(model(0) && !model(1) && !model(2) && model(3))

  /** Restarting at every conflict exercises a restart right after a unit clause
   *  is learned, when its literal is on the trail but not yet propagated: it
   *  used to be skipped, never reaching the theory (found on a QF_UFLIA
   *  instance).
   */
  test("random problems with a theory agree with brute force, restarting at every conflict"):
    val rnd = scala.util.Random(5)
    for _ <- 0 until 300 do
      val n = 4 + rnd.nextInt(6)
      val m = (2.5 * n).toInt
      val clauses = Seq.fill(m)(Seq.fill(3)(Lit(rnd.nextInt(n), rnd.nextBoolean())))
      val groups =
        Seq.fill(1 + rnd.nextInt(2))((Seq.fill(2 + rnd.nextInt(3))(rnd.nextInt(n)).distinct, 1 + rnd.nextInt(2)))
      def ok(model: Array[Boolean]) = groups.forall((atoms, k) => atoms.count(model) <= k)
      val solver = Solver(n, AtMostTheory(groups), restartInterval = 1)
      clauses.foreach(c => solver.addClause(c*))
      val result = solver.solve() match
        case Solver.Result.Sat(model) => Some(model)
        case Solver.Result.Unsat => None
      assertEquals(result.isDefined, bruteForce(n, clauses, ok), s"n=$n clauses=$clauses groups=$groups")

  test("random problems with a theory agree with brute force"):
    val rnd = scala.util.Random(4)
    var sats = 0
    for _ <- 0 until 300 do
      val n = 4 + rnd.nextInt(6)
      val m = (2.5 * n).toInt
      val clauses = Seq.fill(m)(Seq.fill(3)(Lit(rnd.nextInt(n), rnd.nextBoolean())))
      val groups =
        Seq.fill(1 + rnd.nextInt(2))((Seq.fill(2 + rnd.nextInt(3))(rnd.nextInt(n)).distinct, 1 + rnd.nextInt(2)))
      def ok(model: Array[Boolean]) = groups.forall((atoms, k) => atoms.count(model) <= k)
      val result = sat(n, clauses, AtMostTheory(groups))
      assertEquals(result.isDefined, bruteForce(n, clauses, ok), s"n=$n clauses=$clauses groups=$groups")
      for model <- result do
        assert(satisfies(model, clauses) && ok(model))
        sats += 1
    assert(sats > 50)
