package net.bovel.bench

import com.microsoft.z3.{Context, Status}
import net.bovel.smtlib.{Corpus, Parser}
import net.bovel.vik.Lowering

/** Checks [[Z3Program]] against the corpus statuses:
 *  `sbt "bench/runMain net.bovel.bench.z3check file.smt2 [more...]"` compiles
 *  each instance, replays it in a fresh context and compares z3's answer with
 *  the file's status — the compiler is exercised exactly as the benchmark
 *  exercises it.
 */
@main def z3check(files: String*): Unit =
  Natives.z3
  var failures = 0
  for file <- files do
    val text = java.nio.file.Files.readString(Corpus.find(file))
    val script = Parser.parse(text)
    val program = Z3Program.compile(Lowering(script))
    val ctx = Context()
    try
      val answers = program.check(ctx, OtherSolversBench.logicOf(text))
      val expected = script.expectedStatuses
      val ok = answers.length == expected.length
        && answers.zip(expected).forall((a, e) => e.forall(_ == a))
      if !ok then failures += 1
      val shown = answers.map(a => if a then "sat" else "unsat").mkString(" ")
      println(s"$file: $shown${if ok then "" else s" EXPECTED $expected"}")
    finally ctx.close()
  if failures > 0 then sys.exit(1)
