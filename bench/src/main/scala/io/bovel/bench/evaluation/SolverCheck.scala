package io.bovel.bench.evaluation

import com.microsoft.z3.Context
import io.bovel.smtlib.{Corpus, Parser}
import io.github.cvc5.Context as Cvc5Context

/** Runs every in-process solver of the evaluation on the SMT-LIB instances of
 *  the corpus taken from SMT-LIB (`corpus/<logic>/smt-lib`) and reports the
 *  answers that differ from the expected status:
 *  `sbt "bench/runMain io.bovel.bench.evaluation.SolverCheck"` (optionally a
 *  regex selecting files among the whole corpus, and solver names among `vik`,
 *  `z3`, `z3Text`, `cvc5`, `cvc5Text`, `princess`, `smtinterpol`).
 */
object SolverCheck:
  def main(args: Array[String]): Unit =
    val filter = args.headOption.getOrElse("/smt-lib/").r
    val solvers =
      if args.length > 1 then args.drop(1).toList
      else List("vik", "z3", "z3Text", "cvc5", "cvc5Text", "princess", "smtinterpol")
    val files =
      Corpus.files.filter(f => filter.findFirstIn(f).isDefined).map(f => java.nio.file.Path.of(f).getFileName.toString)
    Natives.z3
    for file <- files do
      try check(file, solvers)
      catch
        case error: Throwable => // the replay's limits
          limits += 1
          println(s"$file: not checked: ${error.getMessage}")
    println(s"${files.length} files, $disagreements with a disagreement, $limits at a solver's or the replay's limit")
    if disagreements > 0 then sys.exit(1) // a check: the exit code says, for CI

  private var disagreements = 0 // files where an answer is against a status or another solver's
  private var limits = 0 // files a solver or the replay could not take: not a disagreement

  /** Seconds a solver has per file: `VIK_CHECK_SECONDS`, 30 by default. */
  private val limit = sys.env.get("VIK_CHECK_SECONDS").map(_.toLong).getOrElse(30L)
  private val pool = java.util.concurrent.Executors.newCachedThreadPool { r =>
    val t = Thread(r)
    t.setDaemon(true)
    t
  }

  private def timed(name: String, run: () => List[Boolean]): List[Boolean] =
    val task = pool.submit(() => run())
    try task.get(limit, java.util.concurrent.TimeUnit.SECONDS)
    catch
      case _: java.util.concurrent.TimeoutException => throw RuntimeException(s"over $limit s")
      case e: java.util.concurrent.ExecutionException => throw e.getCause

  private def check(file: String, solvers: List[String]): Unit =
    val instance = Instance(file)
    // Every check-sat of the script, against its own status where the script has one, and against
    // the other solvers everywhere: a file without a status is still a disagreement between solvers,
    // and a script of several checks is compared check by check, not its last answer to its first
    // status (issue 34).
    val expected = Parser.parse(instance.text).expectedStatuses
    // A solver gets `limit` seconds per file, on a thread of its own that a solver past the limit
    // keeps (the check tool does not wait on a solver that would not answer): reported as such.
    val answers: List[(String, List[Boolean] | String)] = solvers.map { name =>
      val got: List[Boolean] | String =
        try
          timed(
            name,
            () =>
              name match
                case "vik" => VikReplay.check(instance.replay)
                case "z3" =>
                  val ctx = Context()
                  try Z3Replay.check(instance.replay, ctx)
                  finally ctx.close()
                case "z3Text" => TextOracle.z3(instance.text)
                case "cvc5" =>
                  try Cvc5Replay.check(instance.replay)
                  finally Cvc5Context.deletePointers()
                case "cvc5Text" =>
                  try TextOracle.cvc5(instance.text)
                  finally Cvc5Context.deletePointers()
                case "princess" => PrincessReplay.check(instance.replay)
                case "smtinterpol" => SmtInterpolReplay.check(instance.replay)
          )
        catch case error: Throwable => String.valueOf(error.getMessage) // gave up or broke: reported, not fatal
      (name, got)
    }
    def word(b: Boolean) = if b then "sat" else "unsat"
    val broken = answers.collect { case (n, message: String) => s"$n: $message" }
    val lists = answers.collect { case (n, l: List[Boolean]) => (n, l) }
    val checks = (expected.length :: lists.map(_._2.length)).max
    val wrong = (0 until checks).flatMap { i =>
      val stamp = expected.lift(i).flatten
      val said = lists.flatMap((n, l) => l.lift(i).map(n -> _))
      val againstStamp = said.filter((_, b) => stamp.exists(_ != b)).map((n, b) => s"$n says ${word(b)}")
      val amongThemselves =
        if stamp.isEmpty && said.map(_._2).distinct.length > 1 then said.map((n, b) => s"$n says ${word(b)}")
        else Nil
      val short = lists.filter(_._2.length <= i).map((n, _) => s"$n stops")
      val issues = againstStamp ++ amongThemselves ++ short
      if issues.isEmpty then Nil
      else List(s"check ${i + 1} (${stamp.map(word).getOrElse("no status")}): ${issues.mkString(", ")}")
    }
    if wrong.nonEmpty then disagreements += 1
    else if broken.nonEmpty then limits += 1
    if wrong.nonEmpty || broken.nonEmpty then println(s"$file: ${(wrong ++ broken).mkString("; ")}")
