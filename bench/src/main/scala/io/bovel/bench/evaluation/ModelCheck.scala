package io.bovel.bench.evaluation

import io.bovel.smtlib.{Corpus, Parser}
import io.bovel.smtlib.sexpr.{SExpr, SExprParser}
import io.bovel.smtlib.sexpr.SExpr.{SList, SSymbol}
import io.bovel.vik.Session

/** Checks VIK's models against z3: for every `get-model` of the selected corpus
 *  files, the script up to its `check-sat` with the model's definitions in
 *  place of the declarations they value, and the abstract elements declared and
 *  told apart, must be `sat` for z3 — a model that contradicts its script is
 *  not (issue 35): `sbt "bench/runMain io.bovel.bench.evaluation.ModelCheck"`
 *  (optionally a regex selecting files; every file with a `get-model`
 *  otherwise).
 */
object ModelCheck:
  def main(args: Array[String]): Unit =
    val filter = args.headOption.getOrElse("").r
    val files = Corpus.files.filter(f => filter.findFirstIn(f).isDefined && Corpus.read(f).contains("(get-model)"))
    Natives.z3 // the binding's library, loaded before z3 is asked anything
    var checked = 0
    var wrong = 0
    var unchecked = 0
    for file <- files do
      val commands = SExprParser.parse(Corpus.read(file))
      for case (SList(SSymbol("get-model") :: Nil), i) <- commands.zipWithIndex do
        val prefix = commands.take(i + 1)
        val responses = Session.run(Parser.parse(prefix.mkString("\n")))
        // The answer of the last check before the model, past any value queries between the two.
        val lastCheck = responses.init.findLast(r => r == "sat" || r == "unsat" || r == "unknown")
        if lastCheck.contains("sat") then
          checked += 1
          val model = SExprParser.parse(responses.last) match
            case List(SList(definitions)) => definitions
            case _ => Nil
          val defined = model.collect { case d @ SList(SSymbol("define-fun") :: SSymbol(name) :: _) => name -> d }
          verify(file, i, prefix.init, model, defined.toMap) match
            case None => ()
            case Some(reason) =>
              if reason.startsWith("not checked") then unchecked += 1 else wrong += 1
              println(s"$file, get-model ${i + 1}: $reason")
    println(s"${checked - unchecked} models checked, $wrong wrong, $unchecked not checkable by z3")

  /** The script with the model in it, for z3; the reason when z3 does not find
   *  it satisfiable.
   */
  private def verify(
      file: String,
      at: Int,
      script: List[SExpr],
      model: List[SExpr],
      defined: Map[String, SExpr]
  ): Option[String] =
    // The abstract elements the model names, `@Sort!k`, declared and distinct.
    val elements = model.flatMap(symbols).filter(_.startsWith("@")).distinct
    val declared =
      elements.map(e => SList(List(SSymbol("declare-const"), SSymbol(e), SSymbol(e.drop(1).takeWhile(_ != '!')))))
    val apart = elements.groupBy(e => e.drop(1).takeWhile(_ != '!')).values.filter(_.length > 1).map(es =>
      SList(List(SSymbol("assert"), SList(SSymbol("distinct") :: es.map(SSymbol(_)))))
    )
    val rewritten = script.flatMap {
      case SList(SSymbol("declare-fun" | "declare-const") :: SSymbol(name) :: _) if defined.contains(name) =>
        List(defined(name))
      // The options stay: `:global-declarations` keeps the declarations across a `reset-assertions`.
      case SList(SSymbol("get-value" | "get-model" | "get-unsat-core" | "get-assertions" | "get-info") :: _) => Nil
      case other => List(other)
    }
    // The elements after the sorts and datatypes they are of, before anything that mentions them.
    val (sorts, rest) = rewritten.span {
      case SList(SSymbol(
            "set-logic" | "set-option" | "set-info" | "declare-sort" | "declare-datatype" | "declare-datatypes" | "define-sort"
          ) :: _) => true
      case _ => false
    }
    val text = (sorts ++ declared ++ apart ++ rest).mkString("\n")
    val original = script.filterNot {
      case SList(SSymbol("get-value" | "get-model" | "get-unsat-core" | "get-assertions" | "get-info") :: _) => true
      case _ => false
    }.mkString("\n")
    if scala.util.Try(TextOracle.z3Binary(original)).isFailure then
      return Some("not checked: z3 rejects the script itself (a sort of its own, say)")
    scala.util.Try(TextOracle.z3Binary(text)) match
      case scala.util.Success(list) if list.lastOption.contains(true) => None
      case scala.util.Success(list) =>
        Some(s"z3 answers ${list.map(b => if b then "sat" else "unsat").mkString(", ")} with the model in place")
      case scala.util.Failure(error) => Some(s"z3 rejects the script with the model in place: ${error.getMessage}")

  private def symbols(s: SExpr): List[String] = s match
    case SSymbol(name) => List(name)
    case SList(items) => items.flatMap(symbols)
    case _ => Nil
