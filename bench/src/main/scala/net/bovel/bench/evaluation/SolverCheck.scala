package net.bovel.bench.evaluation

import com.microsoft.z3.Context
import io.github.cvc5.Context as Cvc5Context
import net.bovel.smtlib.{Corpus, Parser}

/** Runs every in-process solver of the evaluation on the SMT-LIB instances of
 *  the corpus taken from SMT-LIB (`corpus/<logic>/smt-lib`) and reports the
 *  answers that differ from the expected status:
 *  `sbt "bench/runMain net.bovel.bench.evaluation.SolverCheck"` (optionally a
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
    var disagreements = 0
    for file <- files do
      val instance = Instance(file)
      val expected = Parser.parse(instance.text).expectedStatus
      val answers = solvers.map { name =>
        val sat = try name match
          case "vik" => VikReplay.check(instance.replay).last
          case "z3" =>
            val ctx = Context()
            try Z3Replay.check(instance.replay, ctx).last
            finally ctx.close()
          case "z3Text" => TextOracle.z3(instance.text).last
          case "cvc5" =>
            try Cvc5Replay.check(instance.replay).last
            finally Cvc5Context.deletePointers()
          case "cvc5Text" =>
            try TextOracle.cvc5(instance.text).last
            finally Cvc5Context.deletePointers()
          case "princess" => PrincessReplay.check(instance.replay).last
          case "smtinterpol" => SmtInterpolReplay.check(instance.replay).last
        catch case error: Throwable => error.getMessage // a solver that gave up or broke: reported, not fatal
        (name, sat)
      }
      val wrong = answers.filter {
        case (_, sat: Boolean) => expected.exists(_ != sat)
        case _ => true
      }
      if wrong.nonEmpty then
        disagreements += wrong.length
        println(s"$file: expected ${expected.map(if _ then "sat" else "unsat").getOrElse("?")}, " +
          wrong.map {
            case (n, sat: Boolean) => s"$n says ${if sat then "sat" else "unsat"}"
            case (n, message) => s"$n: $message"
          }.mkString(", "))
    println(s"${files.length} files, $disagreements disagreements")
