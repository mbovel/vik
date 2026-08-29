package net.bovel.bench

import scala.io.Source

/** Runs every in-process solver of [[OtherSolversBench]] on every SMT-LIB
 *  instance of the benchmarks and reports the answers that differ from the
 *  expected status: `sbt "bench/runMain net.bovel.bench.SolverCheck"`
 *  (optionally a regex selecting files, and solver names).
 */
object SolverCheck:
  def main(args: Array[String]): Unit =
    val filter = args.headOption.getOrElse(".").r
    val solvers =
      if args.length > 1 then args.drop(1).toList else List("vik", "z3", "z3Direct", "cvc5", "princess", "smtinterpol")
    val files = Source.fromResource("smtlib/index.txt").getLines().toList.filter(f => filter.findFirstIn(f).isDefined)
    var disagreements = 0
    for file <- files do
      val bench = OtherSolversBench()
      bench.file = file
      bench.setup()
      val expected = net.bovel.smt2.Parser.parse(Source.fromResource(s"smtlib/$file").mkString).expectedStatus
      val answers = solvers.map { name =>
        val sat = name match
          case "vik" => bench.vik().last
          case "z3" => bench.z3()
          case "z3Direct" => bench.z3Direct()
          case "cvc5" => bench.cvc5().last
          case "princess" => bench.princess()
          case "smtinterpol" => bench.smtinterpol().last
        (name, sat)
      }
      val wrong = answers.filter((_, sat) => expected.exists(_ != sat))
      if wrong.nonEmpty then disagreements += 1
      println(f"$file%-70s ${expected.map(if _ then "sat" else "unsat").getOrElse("?")}%-6s " +
        answers.map((n, s) => s"$n=${if s then "sat" else "unsat"}").mkString(" ") +
        (if wrong.nonEmpty then "  <-- DISAGREEMENT" else ""))
    println(s"${files.length} files, $disagreements with a disagreement")
