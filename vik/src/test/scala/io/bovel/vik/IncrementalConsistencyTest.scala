package io.bovel.vik

import io.bovel.smtlib.Parser

/** Random incremental scripts against the one-shot oracle: after every
 *  `check-sat`, a fresh solver given just the live assertions must agree.
 *  Anything leaking across solves or levels — a popped fact surviving, a clause
 *  lost with a retired level, a search stopping short — shows up as a
 *  disagreement.
 */
class IncrementalConsistencyTest extends munit.FunSuite:
  private val declarations = """
    (declare-const x Int)
    (declare-const y Int)
    (declare-fun f (Int) Int)
    (declare-fun p (Int) Bool)
  """

  private val fuel = 200000L

  test("push, pop and re-solve agree with solving from scratch"):
    val random = scala.util.Random(20260901)
    def num(n: Int): String = if n < 0 then s"(- ${-n})" else n.toString
    def simple(): String =
      random.nextInt(8) match
        case 0 => s"(< x ${num(random.nextInt(7) - 2)})"
        case 1 => s"(> x ${num(random.nextInt(7) - 2)})"
        case 2 => s"(< y ${num(random.nextInt(7) - 2)})"
        case 3 => s"(= (f x) ${random.nextInt(4)})"
        case 4 => s"(= (f y) ${random.nextInt(4)})"
        case 5 => "(= x y)"
        case 6 => "(p x)"
        case _ => "(not (p y))"
    def atom(): String = if random.nextInt(4) == 0 then s"(or ${simple()} ${simple()})" else simple()
    agreeWithScratch(declarations, atom, random, scripts = 60, steps = 8)

  test("the constants the lowering mints do not outlive their definitions"):
    val random = scala.util.Random(20261109)
    val bitVectors = """
      (declare-const x (_ BitVec 4))
      (declare-const y (_ BitVec 4))
      (declare-const r Real)
      (declare-fun f (Int) (_ BitVec 4))
    """
    def pick(choices: String*): String = choices(random.nextInt(choices.length))
    // Few terms, so that one first used under a level comes back after its pop.
    def bv(): String =
      pick(
        "x",
        "y",
        "(f 0)",
        "(bvadd x #x1)",
        "(bvmul y #x3)",
        "(bvand x y)",
        "(bvudiv x y)",
        "(ite (bvult x y) x y)",
        "#x2"
      )
    def rational(): String = pick("0.5", "0.25", "(* 0.5 r)")
    def atom(): String = pick(
      s"(= ${bv()} ${bv()})",
      s"(not (= ${bv()} ${bv()}))",
      s"(bvult ${bv()} ${bv()})",
      s"(= r ${rational()})",
      s"(< r ${rational()})",
      s"(> r ${rational()})"
    )
    agreeWithScratch(bitVectors, atom, random, scripts = 300, steps = 16)

  /** Random scripts over the atoms of `atom`, each `check-sat` against solving
   *  just the live assertions from scratch.
   */
  private def agreeWithScratch(
      declarations: String,
      atom: () => String,
      random: scala.util.Random,
      scripts: Int,
      steps: Int
  ): Unit =
    for scriptIndex <- 1 to scripts do
      val live = collection.mutable.ArrayBuffer(collection.mutable.ArrayBuffer.empty[String])
      val commands = StringBuilder(declarations)
      val checks = collection.mutable.ArrayBuffer.empty[List[String]]
      def check(): Unit =
        checks += live.toList.flatten
        commands ++= "(check-sat)\n"
      for _ <- 1 to steps do
        random.nextInt(6) match
          case 0 | 1 | 2 =>
            val a = atom()
            live.last += a
            commands ++= s"(assert $a)\n"
          case 3 =>
            live += collection.mutable.ArrayBuffer.empty
            commands ++= "(push)\n"
          case 4 if live.length > 1 =>
            live.remove(live.length - 1)
            commands ++= "(pop)\n"
          case _ => check()
      check()
      val got = Session.answers(Parser.parse(commands.toString), fuel = fuel)
      assertEquals(got.length, checks.length)
      for (answer, liveAsserts) <- got.zip(checks) do
        val oneShot = declarations + liveAsserts.map(a => s"(assert $a)\n").mkString + "(check-sat)\n"
        val oracle = Session.answers(Parser.parse(oneShot), fuel = fuel).head
        (answer, oracle) match
          case (Some(a), Some(b)) =>
            assert(a == b, s"script $scriptIndex diverges (incremental $a, from scratch $b):\n$commands")
          case _ => () // unknown on either side proves nothing
