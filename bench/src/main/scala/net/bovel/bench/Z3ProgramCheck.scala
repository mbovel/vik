package net.bovel.bench

import com.microsoft.z3.{Context, Status}
import net.bovel.smt2.{Corpus, Parser}

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
    val script = Parser.parse(java.nio.file.Files.readString(Corpus.find(file)))
    val program = Z3Program.compile(script)
    val ctx = Context()
    try
      val solver = ctx.mkSolver()
      solver.add(program.run(ctx)*)
      val answer = solver.check() == Status.SATISFIABLE
      val expected = script.expectedStatus
      val ok = expected.forall(_ == answer)
      if !ok then failures += 1
      println(s"$file: ${if answer then "sat" else "unsat"}${if ok then "" else s" EXPECTED ${expected}"}")
    finally ctx.close()
  if failures > 0 then sys.exit(1)
