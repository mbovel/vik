package io.bovel.vik
import scala.io.Source

import io.bovel.smtlib.{Parser, Unsupported}

private def parseFile(path: String) =
  val source = Source.fromFile(path)
  try Parser.parse(source.mkString)
  finally source.close()

/** The command line, `io.bovel.vik.run`: solves SMT-LIB 2 files, one after the
 *  other in one process, and prints the responses — `sat` or `unsat` for each
 *  `check-sat`, the models and values asked for. One file is the usual call;
 *  several make one process solve them all — what a training run for a JVM's
 *  AOT cache needs (bench/aot.sh).
 */
@main def run(paths: String*): Unit =
  // The responses in UTF-8 whatever the locale: under `LC_ALL=C` HotSpot's standard output wrote the
  // non-ASCII characters of a symbol as `?`, itself a symbol character, so the model defined another
  // symbol than the script's (issue 60).
  val out = java.io.PrintStream(System.out, true, "UTF-8")
  Console.withOut(out) {
    for path <- paths do Session.run(parseFile(path)).foreach(println)
  }
  out.flush()

/** Prints an SMT-LIB 2 file as VIK will play it, after the lowerings:
 *  bit-vectors become bounded integers with their defining equations
 *  ([[Lowering]]), rational literals become defined constants (the parser). A
 *  debugging view.
 */
@main def lower(path: String): Unit = print(Printer.print(Lowering(parseFile(path))))

/** Parses the given SMT-LIB files and prints, per file, `ok` or the error:
 *  whether each is in the supported fragment.
 */
@main def check(paths: String*): Unit =
  for path <- paths do
    val result =
      try
        parseFile(path); "ok"
      catch case e: Exception => s"${e.getClass.getSimpleName}: ${e.getMessage}"
    println(s"$path\t$result")
