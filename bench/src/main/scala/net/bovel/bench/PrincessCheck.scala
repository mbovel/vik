package net.bovel.bench

import java.io.StringReader

import ap.api.SimpleAPI
import net.bovel.smt2.{Corpus, Parser}

/** Checks the princess harness against the corpus statuses:
 *  `sbt "bench/runMain net.bovel.bench.princesscheck file.smt2 [more...]"`
 *  executes each instance as the benchmark does — the script through
 *  `execSMTLIB`, answers captured from the output — and compares them with
 *  the file's statuses.
 */
@main def princesscheck(files: String*): Unit =
  var failures = 0
  for file <- files do
    val text = java.nio.file.Files.readString(Corpus.find(file))
    val expected = Parser.parse(text).expectedStatuses
    val p = SimpleAPI.spawn
    val answers =
      try
        val buffer = java.io.ByteArrayOutputStream()
        Console.withOut(java.io.PrintStream(buffer)) { p.execSMTLIB(StringReader(text)) }
        buffer.toString.linesIterator.collect {
          case "sat" => true
          case "unsat" => false
        }.toList
      finally p.shutDown
    val ok = answers.length == expected.length && answers.zip(expected).forall((a, e) => e.forall(_ == a))
    if !ok then failures += 1
    val shown = answers.map(a => if a then "sat" else "unsat").mkString(" ")
    println(s"$file: $shown${if ok then "" else s" EXPECTED $expected"}")
  if failures > 0 then sys.exit(1)
