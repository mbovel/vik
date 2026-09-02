package net.bovel.bench

import net.bovel.smtlib.Corpus

/** Runs every in-process solver of [[OtherSolversBench]] on the SMT-LIB
 *  instances of the corpus taken from SMT-LIB (`smtlib/<logic>/smt-lib`, the
 *  ones the benchmarks use) and reports the answers that differ from the
 *  expected status: `sbt "bench/runMain net.bovel.bench.SolverCheck"`
 *  (optionally a regex selecting files among the whole corpus, and solver
 *  names).
 */
object SolverCheck:
  def main(args: Array[String]): Unit =
    val filter = args.headOption.getOrElse("/smt-lib/").r
    val solvers =
      if args.length > 1 then args.drop(1).toList else List("vik", "z3", "z3Direct", "cvc5", "princess", "smtinterpol")
    val files =
      Corpus.files.filter(f => filter.findFirstIn(f).isDefined).map(f => java.nio.file.Path.of(f).getFileName.toString)
    var disagreements = 0
    for file <- files do
      val bench = OtherSolversBench()
      bench.file = file
      bench.setup()
      val expected = net.bovel.smtlib.Parser.parse(java.nio.file.Files.readString(Corpus.find(file))).expectedStatus
      val answers = solvers.map { name =>
        val sat = name match
          case "vik" => bench.vik().last
          case "z3" => bench.z3()
          case "z3Direct" => bench.z3Direct().last
          case "cvc5" => bench.cvc5().last
          case "princess" => bench.princess().last
          case "smtinterpol" => bench.smtinterpol().last
        (name, sat)
      }
      val wrong = answers.filter((_, sat) => expected.exists(_ != sat))
      if wrong.nonEmpty then disagreements += 1
      println(f"$file%-70s ${expected.map(if _ then "sat" else "unsat").getOrElse("?")}%-6s " +
        answers.map((n, s) => s"$n=${if s then "sat" else "unsat"}").mkString(" ") +
        (if wrong.nonEmpty then "  <-- DISAGREEMENT" else ""))
    println(s"${files.length} files, $disagreements with a disagreement")
