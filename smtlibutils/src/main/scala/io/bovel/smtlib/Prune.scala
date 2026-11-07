package io.bovel.smtlib

import java.nio.file.{FileVisitResult, Files, Path, SimpleFileVisitor}
import java.nio.file.attribute.BasicFileAttributes
import java.util.concurrent.{Executors, TimeUnit}

import scala.collection.mutable
import scala.jdk.CollectionConverters.*

import io.bovel.smtlib.sexpr.{SExpr, SExprParser}

/** Prunes the corpus, in one of two ways; with `--delete`, the files found go,
 *  with their `ignore.txt` lines. Usage:
 *  `sbt "smtlibutils/runMain io.bovel.smtlib.prune invalid [--delete] [prefix ...]"`,
 *  `sbt "smtlibutils/runMain io.bovel.smtlib.prune alike <level> [--delete] [prefix ...]"`
 *  — the corpus paths to consider (default all).
 *
 *  `invalid`: the files that neither VIK's parser nor z3's reads. Such
 *  a file is no benchmark: its `:status` answers a question the script does
 *  not ask (the Scala 3 dumps that equate an `Any` with a `Function1`, #63). A
 *  file only one of the two refuses stays: VIK refusing what z3 reads is VIK's
 *  gap, z3 refusing what VIK reads is z3's (4.14 lacks `ubv_to_int`). z3 reads
 *  the script without its `check-sat`s, `get-`s and `set-option`s, so that its
 *  errors are about the script, not about solving it or about an option it
 *  does not know (cvc5's `:incremental` is an error to z3); only
 *  `:global-declarations` stays, which changes the scopes. z3 on the PATH.
 *
 *  `alike`: the files alike a file kept (see [[Alike]]): at a level,
 *  `text`, `names` or `order`, those with the canonical form of a file kept; at
 *  a similarity between 0 and 1, those whose shape is at least that similar to
 *  a kept one's. Only files of one logic, with the same statuses and both on
 *  `ignore.txt` or both off it, compare: a sat and an unsat variant of one
 *  problem test more than either, and so does a variant VIK fails beside one
 *  it answers. A file the repository names (the benchmarks' instances, a
 *  test's, the documentation's) is always kept; otherwise the smallest file
 *  is. A summary of every level comes first, and the groups whose canonical
 *  forms are one but whose statuses contradict each other, which no pruning
 *  joins: one of the statuses is wrong, or the canonical form is.
 */
@main def prune(args: String*): Unit =
  val delete = args.contains("--delete")
  val operands = args.drop(1).filter(_ != "--delete")
  val usage = "prune invalid [--delete] [prefix ...] | prune alike <level> [--delete] [prefix ...]"
  val (find, prefixes) = args.headOption match
    case Some("invalid") => (invalid, operands)
    case Some("alike") if operands.nonEmpty => (alike(_, operands.head, _), operands.drop(1))
    case _ => throw IllegalArgumentException(usage)
  val files = Corpus.files.filter(f => prefixes.isEmpty || prefixes.exists(f.startsWith))
  println(s"${files.length} files under ${Corpus.root}")
  // Deep stacks: the readers recurse over the nesting, and the default stack ends some files at a depth that varies.
  val pool = Executors.newFixedThreadPool(Runtime.getRuntime.availableProcessors() min 32, r => Thread(null, r, "prune", 1L << 26))
  try
    val gone = find(files, pool)
    if delete && gone.nonEmpty then
      gone.foreach(f => Files.delete(Corpus.root.resolve(f)))
      val ignore = Corpus.root.resolve("ignore.txt")
      val set = gone.toSet
      val kept = Files.readAllLines(ignore).asScala.filterNot(l => set(l.takeWhile(_ != '\t')))
      Files.writeString(ignore, kept.mkString("", "\n", "\n"))
      println(s"${gone.length} files deleted")
  finally pool.shutdown()

private def invalid(files: List[String], pool: java.util.concurrent.ExecutorService): List[String] =
  val byVik = files.map(f => f -> pool.submit(() => vikError(Corpus.read(f)))).flatMap((f, e) => e.get().map(f -> _))
  val byBoth = byVik.map((f, vik) => (f, vik, pool.submit(() => z3Error(Corpus.read(f)))))
    .flatMap((f, vik, z3) => z3.get().map((f, vik, _)))
  println(s"${byVik.length} refused by VIK's parser, ${byBoth.length} of them by z3 too:")
  for (f, vik, z3) <- byBoth do println(s"  $f\n    vik: $vik\n    z3:  $z3")
  val gone = byBoth.map(_._1)
  println("refused by VIK's parser only:")
  for (f, vik) <- byVik if !gone.contains(f) do println(s"  $f\n    vik: $vik")
  gone

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

/** A corpus file as [[alike]] compares it: what must match first (logic,
 *  statuses, on `ignore.txt` or not), its canonical forms by level, its shape.
 */
private final case class Entry(
    file: String,
    group: (String, List[String], Boolean),
    statuses: List[String],
    size: Int,
    pinned: Boolean,
    digests: Map[Alike.Level, String],
    shape: Alike.Shape
)

private def alike(files: List[String], choice: String, pool: java.util.concurrent.ExecutorService): List[String] =
  val ignored = Corpus.ignored
  val cited = citedFiles()
  val read = files.map(f =>
    f -> pool.submit(() =>
      val text = Corpus.read(f)
      try
        val commands = SExprParser.parse(text)
        val statuses = Alike.statuses(commands)
        val digests = Alike.Level.values.map(l => l -> Alike.digest(commands, l)).toMap
        Some(Entry(
          f,
          (Corpus.logic(f), statuses, ignored.contains(f)),
          statuses,
          text.length,
          cited(Path.of(f).getFileName.toString),
          digests,
          Alike.shape(commands)
        ))
      catch case _: (SExprParser.SyntaxError | StackOverflowError) => None
    )
  )
  val unread = read.collect { case (f, e) if e.get().isEmpty => f }
  // Who is kept when alike: the files the repository names, then the smallest.
  val entries = read.flatMap(_._2.get()).sortBy(e => (!e.pinned, e.size, e.file))
  if unread.nonEmpty then println(s"${unread.length} not read, kept: ${unread.mkString(", ")}")

  /** The files alike an earlier kept one at the level, each with that one. */
  def exact(level: Alike.Level): List[(Entry, Entry)] =
    val first = mutable.HashMap.empty[(Any, String), Entry]
    entries.flatMap { e =>
      val key = (e.group, e.digests(level))
      first.get(key) match
        case Some(kept) if !e.pinned => List(e -> kept)
        case Some(_) => Nil
        case None => first(key) = e; Nil
    }

  /** The files at least `threshold` similar to an earlier kept one. */
  def similar(threshold: Double): List[(Entry, Entry, Double)] =
    val kept = mutable.HashMap.empty[(String, List[String], Boolean), mutable.ArrayBuffer[Entry]]
    entries.flatMap { e =>
      val reps = kept.getOrElseUpdate(e.group, mutable.ArrayBuffer.empty)
      val like = if e.pinned then None
      else reps.iterator.map(r => (r, Alike.similarity(e.shape, r.shape))).find(_._2 >= threshold)
      like match
        case Some((r, s)) => List((e, r, s))
        case None => reps += e; Nil
    }

  val thresholds = List(1.0, 0.95, 0.9, 0.8)
  println(s"${entries.length} files compared; alike a kept one:")
  for level <- Alike.Level.values do println(f"  ${level.toString.toLowerCase}%-8s ${exact(level).length}%5d")
  for t <- thresholds do println(f"  ≥ $t%-6.2f ${similar(t).length}%5d")

  val forms = entries.groupBy(_.digests(Alike.Level.Order)).values
  val split = forms.filter(_.map(_.group._3).distinct.size > 1)
  if split.nonEmpty then
    println(s"${split.size} canonical forms both on and off ignore.txt (VIK answers one copy, not another), all kept:")
    for es <- split do println(es.map(e => s"  ${e.file}${if e.group._3 then " (ignored)" else ""}").mkString("\n"))

  val contradictions = forms.filter(es =>
    es.map(_.statuses).distinct.combinations(2).exists {
      case List(a, b) => a.length == b.length && a.zip(b).exists((x, y) => Set(x, y) == Set("sat", "unsat"))
      case _ => false
    }
  )
  if contradictions.nonEmpty then
    println(s"${contradictions.size} canonical forms with contradicting statuses:")
    for es <- contradictions do println(es.map(e => s"  ${e.file} ${e.statuses.mkString(" ")}").mkString("\n"))

  val found: List[(Entry, Entry, String)] = Alike.Level.values.find(_.toString.equalsIgnoreCase(choice)) match
    case Some(level) => exact(level).map((e, r) => (e, r, ""))
    case None =>
      val t = choice.toDoubleOption.filter(t => t > 0 && t <= 1)
        .getOrElse(throw IllegalArgumentException(s"alike $choice: a level (text, names, order) or a similarity in (0, 1]"))
      similar(t).map((e, r, s) => (e, r, f" ($s%.3f)"))
  println(s"alike $choice: ${found.length} files alike a kept one")
  for (r, es) <- found.groupBy(_._2).toList.sortBy(_._1.file) do
    println(s"  ${r.file}")
    for (e, _, s) <- es.sortBy(_._1.file) do println(s"    ${e.file}$s")
  found.map(_._1.file)

/** The names of the `.smt2` files the repository's sources and documents
 *  mention, outside the corpus files and `ignore.txt`.
 */
private def citedFiles(): Set[String] =
  val repository = Corpus.root.getParent
  val ignore = Corpus.root.resolve("ignore.txt")
  val texts = List(".scala", ".md", ".txt", ".sh", ".py", ".mjs", ".yml")
  val name = """[^\s/"'`()|<>\[\]{},;:*]+\.smt2""".r
  val found = mutable.Set.empty[String]
  Files.walkFileTree(
    repository,
    new SimpleFileVisitor[Path]:
      override def preVisitDirectory(dir: Path, attributes: BasicFileAttributes): FileVisitResult =
        val n = String.valueOf(dir.getFileName)
        if dir != repository && (n.startsWith(".") || n == "target" || n == "node_modules") then FileVisitResult.SKIP_SUBTREE
        else FileVisitResult.CONTINUE
      override def visitFile(file: Path, attributes: BasicFileAttributes): FileVisitResult =
        if file != ignore && texts.exists(file.toString.endsWith) then
          try found ++= name.findAllIn(Files.readString(file))
          catch case _: java.io.IOException => ()
        FileVisitResult.CONTINUE
  )
  found.toSet
