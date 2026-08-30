package net.bovel.smt2

import scala.concurrent.duration.Duration
import scala.util.Try

/** The corpus of SMT-LIB files (`smtlib/`, see its README), one test per file:
 *  the answers must be the statuses in the file, within a fuel per `check-sat`
 *  (conflicts and branches: deterministic, the same on every machine). A file
 *  on `smtlib/ignore.txt` (out of fuel, a wrong answer, an unsupported feature)
 *  is expected to fail, with a tenth of the fuel; when it passes, the test asks
 *  for its line to be removed.
 */
class SmtLibTest extends munit.FunSuite:
  override val munitTimeout = Duration(30, "min")
  private val fuel = 2000000L
  private val ignoredFuel = 1000L // enough to notice a file that became easy
  private val ignored = Corpus.ignored

  private def show(answers: List[Option[Boolean]]): String =
    answers.map(_.map(if _ then "sat" else "unsat").getOrElse("unknown")).mkString(",")

  for file <- Corpus.files do
    test(file):
      val reason = ignored.get(file)
      val outcome = Try {
        val script = Parser.parse(Corpus.read(file))
        val expected = script.expectedStatuses
        val got = Player.answers(script, fuel = if reason.isDefined then ignoredFuel else fuel)
        val agrees = got.length == expected.length &&
          got.zip(expected).forall((g, e) => g.isDefined && e.forall(_ == g.get))
        (agrees, s"expected ${show(expected)}, got ${show(got)}")
      }
      reason match
        case None => assert(outcome.get._1, outcome.get._2)
        case Some(why) =>
          assert(outcome.toOption.forall(!_._1), s"passes now: remove it from smtlib/ignore.txt (was: $why)")
