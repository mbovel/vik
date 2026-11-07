package io.bovel.smtlib

import java.nio.file.Files
import java.util.concurrent.{Executors, TimeUnit}

import scala.jdk.CollectionConverters.*

import io.bovel.smtlib.sexpr.{SExpr, SExprParser}

/** Lists the corpus files that neither VIK's parser nor z3's reads, and with
 *  `--delete` removes them and their `ignore.txt` lines. Such a file is no
 *  benchmark: its `:status` answers a question the script does not ask (the
 *  Scala 3 dumps that equate an `Any` with a `Function1`, #63). A file
 *  only one of the two refuses stays: VIK refusing what z3 reads is VIK's gap,
 *  z3 refusing what VIK reads is z3's (4.14 lacks `ubv_to_int`). z3 reads the
 *  script without its `check-sat`s, `get-`s and `set-option`s, so that its
 *  errors are about the script, not about solving it or about an option it
 *  does not know (cvc5's `:incremental` is an error to z3); only
 *  `:global-declarations` stays, which changes the scopes. Usage:
 *  `sbt "smtlibutils/runMain io.bovel.smtlib.prune [--delete] [prefix ...]"`
 *  — z3 on the PATH, the corpus paths to consider (default all).
 */
@main def prune(args: String*): Unit =
  val delete = args.contains("--delete")
  val prefixes = args.filter(_ != "--delete")
  val files = Corpus.files.filter(f => prefixes.isEmpty || prefixes.exists(f.startsWith))
  println(s"${files.length} files under ${Corpus.root}")

  val pool = Executors.newFixedThreadPool(Runtime.getRuntime.availableProcessors() min 32)
  val byVik = files.map(f => f -> pool.submit(() => vikError(Corpus.read(f)))).flatMap((f, e) => e.get().map(f -> _))
  val byBoth = byVik.map((f, vik) => (f, vik, pool.submit(() => z3Error(Corpus.read(f)))))
    .flatMap((f, vik, z3) => z3.get().map((f, vik, _)))
  pool.shutdown()

  println(s"${byVik.length} refused by VIK's parser, ${byBoth.length} of them by z3 too:")
  for (f, vik, z3) <- byBoth do println(s"  $f\n    vik: $vik\n    z3:  $z3")
  val gone = byBoth.map(_._1).toSet
  println("refused by VIK's parser only:")
  for (f, vik) <- byVik if !gone(f) do println(s"  $f\n    vik: $vik")
  if delete && gone.nonEmpty then
    gone.foreach(f => Files.delete(Corpus.root.resolve(f)))
    val ignore = Corpus.root.resolve("ignore.txt")
    val kept = Files.readAllLines(ignore).asScala.filterNot(l => gone(l.takeWhile(_ != '\t')))
    Files.writeString(ignore, kept.mkString("", "\n", "\n"))
    println(s"${gone.size} files deleted")

/** VIK's parser's error on the script, if it refuses it. A crash is no
 *  refusal: it is VIK's bug, and says nothing of the file.
 */
private def vikError(text: String): Option[String] =
  try
    Parser.parse(text)
    None
  catch
    case e: (Unsupported | SExprParser.SyntaxError) => Some(oneLine(e.getMessage))
    case _: (RuntimeException | StackOverflowError) => None

/** z3's first error on the script without its solving and option commands. */
private def z3Error(text: String): Option[String] =
  val script = Files.createTempFile("prune", ".smt2")
  try
    Files.writeString(script, withoutSolving(text))
    val process = ProcessBuilder("z3", "-smt2", "-T:10", script.toString).redirectErrorStream(true).start()
    val output = new String(process.getInputStream.readAllBytes())
    if !process.waitFor(15, TimeUnit.SECONDS) then process.destroyForcibly()
    "(?s)\\(error \"(.*?)\"\\)".r.findFirstMatchIn(output).map(m => oneLine(m.group(1)))
  finally Files.delete(script)

/** The script without the commands that solve, ask about a solution or set
 *  an option (`check-sat`, `check-sat-assuming`, `get-value`, `get-model`,
 *  `set-option`, ...) but `:global-declarations`, which decides what a name
 *  refers to; as it was when VIK cannot read its S-expressions.
 */
def withoutSolving(text: String): String =
  def solving(s: SExpr) = s match
    case SExpr.SList(SExpr.SSymbol("set-option") :: SExpr.SKeyword(option) :: _) => option != "global-declarations"
    case SExpr.SList(SExpr.SSymbol(c) :: _) => c.startsWith("check-sat") || c.startsWith("get-")
    case _ => false
  try SExprParser.parse(text).filterNot(solving).mkString("", "\n", "\n")
  catch case _: SExprParser.SyntaxError => text

private def oneLine(message: String | Null): String =
  if message == null then "" else message.linesIterator.map(_.trim).mkString(" ")
