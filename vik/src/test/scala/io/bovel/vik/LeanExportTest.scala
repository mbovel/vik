package io.bovel.vik

import io.bovel.smtlib.Parser

/** The proofs of a few `Unsat`s, written as Lean terms, each to a file of
 *  `target/lean-export` for `proofs/lean` to check (`proofs/lean/exported.sh`).
 */
class LeanExportTest extends munit.FunSuite:
  private val out =
    val dir = java.nio.file.Path.of("target", "lean-export")
    java.nio.file.Files.createDirectories(dir)
    Some(dir.toString)

  private def write(name: String, script: String): Unit =
    val session = Session(Solver(proofs = true))
    val answers = session.run(Parser.parse(script))
    assertEquals(answers.lastOption, Some("unsat"))
    val lean = session.solver.leanProof() match
      case Right(text) => text
      case Left(why) => fail(s"no Lean proof: $why")
    for dir <- out do
      val header = s"import Vik\n\nnamespace Vik.Low.Exported.$name\n\nopen Vik.Low Vik.Clausal\n\n"
      java.nio.file.Files.writeString(
        java.nio.file.Path.of(dir, s"$name.lean"),
        header + lean + s"\nend Vik.Low.Exported.$name\n"
      )

  test("bounds"):
    write("Bounds", "(declare-const x Int) (assert (<= x 0)) (assert (< 0 x)) (check-sat)")

  test("propositional"):
    write(
      "Props",
      "(declare-const p Bool) (declare-const q Bool) (assert (or p q)) (assert (not p)) (assert (not q)) (check-sat)"
    )

  test("congruence"):
    write(
      "Congruence",
      "(declare-sort U 0) (declare-fun f (U) U) (declare-const a U) (declare-const b U) " +
        "(assert (= a b)) (assert (not (= (f a) (f b)))) (check-sat)"
    )

  test("levels"):
    write("Levels", "(declare-const x Int) (assert (<= x 0)) (push 1) (assert (< 0 x)) (check-sat)")

  test("assumptions"):
    write(
      "Assuming",
      "(declare-const x Int) (declare-const p Bool) (assert (=> p (< 0 x))) (assert (<= x 0)) (check-sat-assuming (p))"
    )
