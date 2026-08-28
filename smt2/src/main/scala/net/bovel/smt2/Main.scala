package net.bovel.smt2

import scala.io.Source

/** Solves an SMT-LIB 2 file: prints `sat` or `unsat` for each `check-sat`. */
@main def smt2(path: String): Unit =
  val source = Source.fromFile(path)
  val script = try Parser.parse(source.mkString)
  finally source.close()
  for sat <- Player.play(script) do println(if sat then "sat" else "unsat")
