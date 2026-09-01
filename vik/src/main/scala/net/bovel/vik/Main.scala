package net.bovel.vik
import scala.io.Source

import net.bovel.smt2.*

/** Solves an SMT-LIB 2 file: prints `sat` or `unsat` for each `check-sat`. */
@main def smt2(path: String): Unit =
  val source = Source.fromFile(path)
  val script = try Parser.parse(source.mkString)
  finally source.close()
  Player.run(script).foreach(println)

/** Prints an SMT-LIB 2 file as VIK will play it, after the lowerings:
 *  bit-vectors become bounded integers with their defining equations, rational
 *  literals become defined constants. A debugging view.
 */
@main def smt2lowered(path: String): Unit =
  val source = Source.fromFile(path)
  val script = try Parser.parse(source.mkString)
  finally source.close()
  print(Printer.print(script))

/** Parses the given SMT-LIB files and prints, per file, `ok` or the error:
 *  whether each is in the supported fragment.
 */
@main def smt2check(paths: String*): Unit =
  for path <- paths do
    val result =
      try
        Parser.parse(scala.io.Source.fromFile(path).mkString)
        "ok"
      catch case e: Exception => s"${e.getClass.getSimpleName}: ${e.getMessage}"
    println(s"$path\t$result")
