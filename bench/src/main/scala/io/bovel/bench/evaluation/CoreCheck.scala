package io.bovel.bench.evaluation

import io.bovel.smtlib.{Corpus, Parser}
import io.bovel.smtlib.sexpr.{SExpr, SExprParser}
import io.bovel.smtlib.sexpr.SExpr.{SKeyword, SList, SSymbol}
import io.bovel.vik.Session

/** Checks VIK's unsat cores against z3: for every `get-unsat-core` of the
 *  selected corpus files, the script up to its `check-sat` with the named
 *  assertions outside the core dropped and the unnamed ones kept must be
 *  `unsat` for z3 — SMT-LIB's core is a subset of the named assertions that is
 *  unsatisfiable together with the unnamed ones. `sbt "bench/runMain
 *  io.bovel.bench.evaluation.CoreCheck"` (optionally a regex selecting files;
 *  every file with a `get-unsat-core` otherwise).
 */
object CoreCheck:
  def main(args: Array[String]): Unit =
    val filter = args.headOption.getOrElse("").r
    val files =
      Corpus.files.filter(f => filter.findFirstIn(f).isDefined && Corpus.read(f).contains("(get-unsat-core)"))
    Natives.z3
    var checked = 0
    var wrong = 0
    var unchecked = 0
    for file <- files do
      val commands = SExprParser.parse(Corpus.read(file))
      for case (SList(SSymbol("get-unsat-core") :: Nil), i) <- commands.zipWithIndex do
        val prefix = commands.take(i + 1)
        val responses = Session.run(Parser.parse(prefix.mkString("\n")))
        val lastCheck = responses.init.findLast(r => r == "sat" || r == "unsat" || r == "unknown")
        if lastCheck.contains("unsat") && !responses.last.startsWith("(error") then
          checked += 1
          val core = SExprParser.parse(responses.last) match
            case List(SList(names)) => names.collect { case SSymbol(n) => n }.toSet
            case _ => Set.empty[String]
          verify(prefix.init, core) match
            case None => ()
            case Some(reason) =>
              if reason.startsWith("not checked") then unchecked += 1 else wrong += 1
              println(s"$file, get-unsat-core ${i + 1}: $reason")
    println(s"${checked - unchecked} cores checked, $wrong wrong, $unchecked not checkable by z3")

  /** The script with only the core's named assertions, for z3; the reason when
   *  it is not unsat.
   */
  private def verify(script: List[SExpr], core: Set[String]): Option[String] =
    val kept = script.flatMap {
      case SList(SSymbol("assert") :: SList(SSymbol("!") :: body :: attributes) :: Nil) =>
        val name = attributes.sliding(2).collectFirst { case Seq(SKeyword("named"), SSymbol(n)) => n }
        name match
          case Some(n) if !core(n) => Nil // a named assertion outside the core: dropped
          case _ => List(SList(List(SSymbol("assert"), body)))
      // The options stay: `:global-declarations` keeps the declarations across a `reset-assertions`.
      case SList(SSymbol("get-value" | "get-model" | "get-unsat-core" | "get-assertions" | "get-info") :: _) => Nil
      case other => List(other)
    }
    scala.util.Try(TextOracle.z3Binary(kept.mkString("\n"))) match
      case scala.util.Success(list) if list.lastOption.contains(false) => None
      case scala.util.Success(list) =>
        Some(s"z3 answers ${list.map(b => if b then "sat" else "unsat").mkString(", ")} on the core alone: not a core")
      case scala.util.Failure(error) => Some(s"not checked: z3 rejects the script: ${error.getMessage}")
