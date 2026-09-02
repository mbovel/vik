package net.bovel.smtlib

import net.bovel.vik.{Session, Solver, Model}
import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.jdk.CollectionConverters.*
import scala.util.Try

/** The corpus of SMT-LIB files (`corpus/`, see its README), one test per file:
 *  the answers must be the statuses in the file, within a fuel per `check-sat`
 *  (conflicts and branches: deterministic, the same on every machine) and
 *  within five seconds of wall clock — the budget the benchmarks also hold to;
 *  a file over either goes to `corpus/ignore.txt`, nobody waits on it. A file
 *  on `corpus/ignore.txt` (out of fuel or time, a wrong answer, an unsupported
 *  feature) is expected to fail, with a tenth of the fuel; when it passes, the
 *  test asks for its line to be removed.
 */
class SmtLibTest extends munit.FunSuite:
  override val munitTimeout = Duration(30, "min")
  private val fuel = 2000000L
  private val ignoredFuel = 1000L // enough to notice a file that became easy
  private val ignored = Corpus.ignored

  private def show(answers: List[Option[Boolean]]): String =
    answers.map(_.map(if _ then "sat" else "unsat").getOrElse("unknown")).mkString(",")

  test("ignore.txt is sorted"):
    val lines = java.nio.file.Files.readAllLines(Corpus.root.resolve("ignore.txt")).asScala.toList
      .filter(l => l.nonEmpty && !l.startsWith("#"))
    assertEquals(lines, lines.sorted, "corpus/ignore.txt: keep the lines sorted (byte order)")

  for file <- Corpus.files do
    val reason = ignored.get(file)
    test(if reason.exists(_.startsWith("skip")) then file.ignore else munit.TestOptions(file)):
      // The five-second budget, as a wall-clock guard under the fuel: the solver cannot be
      // interrupted, so a run over it is failed from outside and its thread left to the JVM's end.
      val outcome = Try {
        Await.result(
          Future {
            val text = Corpus.read(file)
            val script = Parser.parse(text)
            val expected = script.expectedStatuses
            val got = Session.answers(script, fuel = if reason.isDefined then ignoredFuel else fuel)
            val agrees = got.length == expected.length &&
              got.zip(expected).forall((g, e) => g.isDefined && e.forall(_ == g.get))
            // A file with `; EXPECT:` lines (stamped by `expectstamp`) holds every response to them.
            val expects = text.linesIterator.filter(_.startsWith("; EXPECT: ")).map(_.drop(10)).toList
            val responses =
              if expects.isEmpty || reason.isDefined ||
                !List("(get-value", "(get-model", "(get-unsat-core", "(get-assertions").exists(text.contains)
              then expects
              else Session.run(script, fuel = fuel)
            (agrees && responses == expects, s"expected ${show(expected)}, got ${show(got)}")
          },
          Duration(5, "s")
        )
      }
      reason match
        case None => assert(outcome.get._1, outcome.get._2)
        case Some(why) =>
          assert(outcome.toOption.forall(!_._1), s"passes now: remove it from corpus/ignore.txt (was: $why)")
