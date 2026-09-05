package io.bovel.bench.evaluation

import com.microsoft.z3.Context
import io.github.cvc5.Context as Cvc5Context
import io.bovel.smtlib.{Corpus, Parser}

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
      catch case error: Throwable => println(s"$file: not checked: ${error.getMessage}") // the replay's limits
    println(s"${files.length} files, $disagreements with a disagreement")

  private var disagreements = 0

  private def check(file: String, solvers: List[String]): Unit =
      val instance = Instance(file)
      // Every check-sat of the script, against its own status where the script has one, and against
      // the other solvers everywhere: a file without a status is still a disagreement between solvers,
      // and a script of several checks is compared check by check, not its last answer to its first
      // status (issue 34).
      val expected = Parser.parse(instance.text).expectedStatuses
      val answers: List[(String, List[Boolean] | String)] = solvers.map { name =>
        val got: List[Boolean] | String =
          try name match
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
      if wrong.nonEmpty || broken.nonEmpty then
        disagreements += 1
        println(s"$file: ${(wrong ++ broken).mkString("; ")}")
