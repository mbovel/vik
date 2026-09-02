package net.bovel.bench

import net.bovel.smtlib.{Corpus, Parser}
import net.bovel.vik.Lowering

/** Checks [[SmtInterpolProgram]] against the corpus statuses, as
 *  `princesscheck` does for princess:
 *  `sbt "bench/runMain net.bovel.bench.interpolcheck file.smt2 [more...]"`.
 */
@main def interpolcheck(files: String*): Unit =
  var failures = 0
  for file <- files do
    val script = Parser.parse(java.nio.file.Files.readString(Corpus.find(file)))
    val answers = SmtInterpolProgram.compile(Lowering(script)).check()
    val expected = script.expectedStatuses
    val ok = answers.length == expected.length && answers.zip(expected).forall((a, e) => e.forall(_ == a))
    if !ok then failures += 1
    val shown = answers.map(a => if a then "sat" else "unsat").mkString(" ")
    println(s"$file: $shown${if ok then "" else s" EXPECTED $expected"}")
  if failures > 0 then sys.exit(1)
