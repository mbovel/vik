package io.bovel.smtlib

import java.nio.file.{FileVisitResult, Files, Path, SimpleFileVisitor}
import java.nio.file.attribute.BasicFileAttributes
import java.util.concurrent.{Executors, TimeUnit}

import scala.collection.mutable
import scala.jdk.CollectionConverters.*

import io.bovel.smtlib.sexpr.{SExpr, SExprParser}

/** Prunes the corpus, in one of four ways, and relabels it, in a fifth; with
 *  `--delete`, the files found go, with their `ignore.txt` lines. Usage:
 *  `sbt "smtlibutils/runMain io.bovel.smtlib.prune invalid [--delete] [prefix ...]"`,
 *  `sbt "smtlibutils/runMain io.bovel.smtlib.prune alike <text|names|order|similarity> [--delete] [prefix ...]"`,
 *  `sbt "smtlibutils/runMain io.bovel.smtlib.prune trivial <fuel> <keep> [--delete] prefix ..."`,
 *  `sbt "smtlibutils/runMain io.bovel.smtlib.prune unused <theory> [--delete] prefix ..."`,
 *  `sbt "smtlibutils/runMain io.bovel.smtlib.prune logic [--move] [prefix ...]"`
 *  — the corpus paths to consider (default all, but for `trivial` and `unused`).
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
 *  `text`, `names` or `order`, those with the canonical form of a file kept;
 *  given a number in place of the level, a similarity in (0, 1] such as
 *  `prune alike 0.9`, those whose shape is at least that similar to a kept
 *  one's. Only files of one logic, with the same statuses and both on
 *  `ignore.txt` or both off it, compare: a sat and an unsat variant of one
 *  problem test more than either, and so does a variant VIK fails beside one
 *  it answers. A file the repository names (the benchmarks' instances, a
 *  test's, the documentation's) is always kept; otherwise the smallest file
 *  is. A summary of every level comes first, and the groups whose canonical
 *  forms are one but whose statuses contradict each other, which no pruning
 *  joins: one of the statuses is wrong, or the canonical form is.
 *
 *  `trivial`: in each family, the trivial files beyond `keep`. A file is
 *  trivial when VIK gives its statuses within `fuel` (`Session.spent`) and z3 gives them
 *  without a conflict (in any of its statistics), so that what is easy for
 *  VIK's preprocessing alone stays; a wrong answer, however quick, is no
 *  reason to go. The family is the file's directory and
 *  its name up to `__` (the importer's families) or up to its hash (a Scala 3
 *  dump's test). The prefixes are required: the regression tests are small and
 *  easy on purpose, and the release samples were chosen for their spread. See
 *  [[spread]] for the files kept.
 *
 *  `unused`: the files that apply no operation of the theory (`bv`, `arith`,
 *  `array`; see [[Alike.operations]]), such as the bit-vector Scala 3 dumps
 *  that only declare bit-vector symbols and compare them for equality: their
 *  integer twin asks the same. The prefixes are required, as for `trivial`; a
 *  file on `ignore.txt` stays, since what VIK fails there is the encoding
 *  itself, and so does a file the repository names.
 *
 *  `logic`: the files whose `set-logic` or directory is not the smallest
 *  standard logic they fit ([[Logic.smallest]], which the importer files by
 *  too); with `--move`, each gets that `set-logic`, with a header line saying
 *  so, and goes to that logic's directory, its `ignore.txt` line and the paths
 *  the repository cites it by following it.
 */
@main def prune(args: String*): Unit =
  val delete = args.contains("--delete")
  val move = args.contains("--move")
  val operands = args.drop(1).filter(a => a != "--delete" && a != "--move")
  val usage = "prune invalid [--delete] [prefix ...] | " +
    "prune alike <text|names|order|similarity> [--delete] [prefix ...] | " +
    "prune trivial <fuel> <keep> [--delete] prefix ... | prune unused <theory> [--delete] prefix ... | " +
    "prune logic [--move] [prefix ...]"
  val (find, prefixes) = args.headOption match
    case Some("invalid") => (invalid, operands)
    case Some("alike") if operands.nonEmpty => (alike(_, operands.head, _), operands.drop(1))
    case Some("trivial") if operands.length > 2 && operands(0).toLongOption.isDefined && operands(1).toIntOption.isDefined =>
      (trivial(_, operands(0).toLong, operands(1).toInt, _), operands.drop(2))
    case Some("unused") if operands.length > 1 && theories.contains(operands(0)) =>
      (unused(_, operands(0), _), operands.drop(1))
    case Some("logic") if !delete => (logics(_, move, _), operands)
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
private[smtlib] def vikError(text: String): Option[String] =
  try
    Parser.parse(text)
    None
  catch
    case e: (Unsupported | SExprParser.SyntaxError) => Some(oneLine(e.getMessage))
    case _: (RuntimeException | StackOverflowError) => None

/** z3's first error on the script without its solving and option commands. */
private[smtlib] def z3Error(text: String): Option[String] =
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

/** A file of a family as [[trivial]] weighs it: `easy` when VIK gives its
 *  statuses within the fuel and z3 without a conflict.
 */
private[smtlib] final case class Member(
    file: String,
    statuses: List[String],
    size: Int,
    pinned: Boolean,
    easy: Boolean,
    shape: Alike.Shape
)

private def trivial(files: List[String], fuel: Long, keep: Int, pool: java.util.concurrent.ExecutorService): List[String] =
  val ignored = Corpus.ignored
  val cited = citedFiles()
  val read = files.map(f =>
    f -> pool.submit(() =>
      val text = Corpus.read(f)
      try
        val commands = SExprParser.parse(text)
        val statuses = Alike.statuses(commands)
        val easy = !ignored.contains(f) && statuses.forall(s => s == "sat" || s == "unsat") &&
          io.bovel.vik.Session.answers(Parser.parse(text), fuel = fuel, deadline = Some(System.nanoTime + 5_000_000_000L))
            .map(_.map(if _ then "sat" else "unsat")) == statuses.map(Some(_)) && z3Easy(text, statuses)
        Some(Member(f, statuses, text.length, cited(Path.of(f).getFileName.toString), easy, Alike.shape(commands)))
      catch case _: (Unsupported | SExprParser.SyntaxError | StackOverflowError) => None
    )
  )
  val members = read.flatMap(_._2.get())
  val families = members.groupBy(m => family(m.file)).toList.sortBy(_._1)
  println(s"${members.length} files read, ${members.count(_.easy)} trivial (VIK within $fuel of fuel, z3 without a conflict), " +
    s"${families.length} families; trivial beyond the first k of their family:")
  for k <- List(1, 3, 5, 10) do
    println(f"  k = $k%-3d ${families.map((_, ms) => (ms.count(m => m.easy && !m.pinned) - (k - ms.count(m => m.easy && m.pinned)).max(0)).max(0)).sum}%5d")
  val gone = families.flatMap { (f, ms) =>
    val (easy, others) = ms.partition(m => m.easy && !m.pinned)
    val kept = spread(others, easy, keep - ms.count(m => m.easy && m.pinned))
    easy.filterNot(kept.contains).map(f -> _)
  }
  println(s"trivial $fuel $keep: ${gone.length} trivial files beyond the $keep kept in their family")
  for ((dir, name), ms) <- gone.groupBy(_._1).toList.sortBy(_._1) do
    println(s"  $dir/$name: ${ms.length} of ${families.find(_._1 == (dir, name)).fold(0)(_._2.length)}")
    for (_, m) <- ms.sortBy(_._2.file) do println(s"    ${m.file}")
  gone.map(_._2.file)

/** Whether z3 gives the statuses with no conflict: it reads the script without
 *  them (it errors on a status it contradicts), and `-st` names a statistic
 *  only when it is not zero.
 */
private def z3Easy(text: String, statuses: List[String]): Boolean =
  val script = Files.createTempFile("prune", ".smt2")
  try
    Files.writeString(script, withoutStatuses(text))
    val process = ProcessBuilder("z3", "-smt2", "-st", "-T:10", script.toString).redirectErrorStream(true).start()
    val output = new String(process.getInputStream.readAllBytes())
    if !process.waitFor(15, TimeUnit.SECONDS) then process.destroyForcibly()
    val lines = output.linesIterator.map(_.trim).toList
    !lines.exists(_.startsWith("(error")) && lines.filter(l => l == "sat" || l == "unsat" || l == "unknown") == statuses &&
    "conflicts\\s+[1-9]".r.findFirstIn(output).isEmpty
  finally Files.delete(script)

/** A file's family: its directory, and its name up to `__` or up to its hash. */
private[smtlib] def family(file: String): (String, String) =
  val path = Path.of(file)
  val name = path.getFileName.toString
  val stem = if name.contains("__") then name.take(name.indexOf("__")) else name.replaceFirst("-[0-9a-f]+\\.smt2$", "")
  (String.valueOf(path.getParent), stem)

/** The `n` candidates to keep beside the files kept anyway: each in turn the
 *  candidate least like any file kept so far (farthest-point sampling over the
 *  shapes, two files of different statuses wholly unlike), the largest on a
 *  tie — so a family keeps its sat and its unsat variants, and its odd ones,
 *  before a second copy of anything.
 */
private[smtlib] def spread(kept: List[Member], candidates: List[Member], n: Int): List[Member] =
  def like(a: Member, b: Member) = if a.statuses != b.statuses then 0.0 else Alike.similarity(a.shape, b.shape)
  val chosen = mutable.ListBuffer.empty[Member]
  var left = candidates
  while chosen.length < n && left.nonEmpty do
    val next = left.minBy(c => ((kept ++ chosen).map(like(c, _)).maxOption.getOrElse(0.0), -c.size, c.file))
    chosen += next
    left = left.filterNot(_ eq next)
  chosen.toList

/** The operations of each theory `unused` knows. */
private val theories: Map[String, String => Boolean] =
  val bitVector = Set("concat", "extract", "repeat", "zero_extend", "sign_extend", "rotate_left", "rotate_right") ++
    Set("ubv_to_int", "sbv_to_int", "int_to_bv", "nat2bv", "int2bv")
  Map(
    "bv" -> (op => op.startsWith("bv") || bitVector(op)),
    "arith" -> Set("+", "-", "*", "/", "div", "mod", "abs", "<", "<=", ">", ">=", "to_real", "to_int", "is_int"),
    "array" -> Set("select", "store", "const")
  )

private def unused(files: List[String], theory: String, pool: java.util.concurrent.ExecutorService): List[String] =
  val ignored = Corpus.ignored
  val cited = citedFiles()
  val read = files.map(f =>
    f -> pool.submit(() =>
      try Some(Alike.operations(SExprParser.parse(Corpus.read(f))))
      catch case _: (SExprParser.SyntaxError | StackOverflowError) => None
    )
  )
  val operations = read.flatMap((f, ops) => ops.get().map(f -> _))
  println(s"${operations.length} files read; applying no operation of " +
    theories.toList.sortBy(_._1).map((t, of) => s"$t: ${operations.count((_, ops) => !ops.exists(of))}").mkString(", "))
  val (none, any) = operations.partition((_, ops) => !ops.exists(theories(theory)))
  val (kept, gone) = none.map(_._1).partition(f => ignored.contains(f) || cited(Path.of(f).getFileName.toString))
  println(s"unused $theory: ${gone.length} files apply no $theory operation" +
    (if kept.isEmpty then "" else s" (and ${kept.length} more kept: on ignore.txt or named by the repository)"))
  for f <- gone do println(s"  $f")
  gone

private def logics(files: List[String], move: Boolean, pool: java.util.concurrent.ExecutorService): List[String] =
  val read = files.map(f =>
    f -> pool.submit(() =>
      try Logic.smallest(Corpus.read(f))
      catch case _: (SExprParser.SyntaxError | StackOverflowError) => None
    )
  )
  // The files whose `set-logic` or directory is not the smallest logic they fit.
  val found = read.flatMap((f, l) => l.get().map(f -> _)).map((f, to) => (f, Logic.claimed(Corpus.read(f)), to))
    .filter((f, claimed, to) => claimed != List(to) || Path.of(f).getName(0).toString != to)
  def said(claimed: List[String]) = claimed.headOption.getOrElse("none")
  println(s"${found.length} files whose set-logic or directory is not the smallest logic they fit:")
  for ((from, dir, to), fs) <- found.groupBy((f, c, to) => (said(c), Path.of(f).getName(0).toString, to)).toList
      .sortBy((k, fs) => (-fs.length, k))
  do println(f"  ${from}%-12s in ${dir}%-12s -> $to%-12s ${fs.length}%5d")
  for (f, _, to) <- found do println(s"    $f: $to")
  if move then
    val moved = found.flatMap { (f, claimed, to) =>
      val text = Corpus.read(f)
      val target = Path.of(to).resolve(Path.of(f).subpath(1, Path.of(f).getNameCount)).toString
      if target != f && Files.exists(Corpus.root.resolve(target)) then
        println(s"  $f stays: $target exists")
        None
      else
        Files.createDirectories(Corpus.root.resolve(target).getParent)
        Files.writeString(Corpus.root.resolve(target), if claimed == List(to) then text else Logic.relabel(text, to))
        if target != f then Files.delete(Corpus.root.resolve(f))
        Some(f -> target)
    }.toMap
    val ignore = Corpus.root.resolve("ignore.txt")
    val (comments, entries) = Files.readAllLines(ignore).asScala.toList.partition(_.startsWith("#"))
    val renamed = entries.map(l => moved.get(l.takeWhile(_ != '\t')).fold(l)(t => t + l.dropWhile(_ != '\t')))
    Files.writeString(ignore, (comments ++ renamed.sorted).mkString("", "\n", "\n"))
    // A path the repository cites (`bench/evaluation-instances.txt`, the CI) follows its file; a name alone still finds it.
    val paths = moved.filter(_ != _).map((f, t) => s"corpus/$f" -> s"corpus/$t")
    val cited = repositoryTexts().filter(p => !p.startsWith(Corpus.root)).flatMap { p =>
      val text = Files.readString(p)
      val changed = paths.foldLeft(text)((t, path) => t.replace(path._1, path._2))
      Option.when(changed != text) { Files.writeString(p, changed); Corpus.root.getParent.relativize(p) }
    }
    println(s"${moved.count(_ != _)} files moved, ${moved.count(_ == _)} relabeled in place" +
      (if cited.isEmpty then "" else s"; their paths changed in ${cited.mkString(", ")}"))
  Nil

/** The names of the `.smt2` files the repository's sources and documents
 *  mention, outside the corpus files and `ignore.txt`.
 */
private def citedFiles(): Set[String] =
  val ignore = Corpus.root.resolve("ignore.txt")
  val name = """[^\s/"'`()|<>\[\]{},;:*]+\.smt2""".r
  repositoryTexts().filter(_ != ignore).flatMap(p => name.findAllIn(Files.readString(p))).toSet

/** The repository's sources and documents: its Scala, Markdown, text,
 *  shell, Python, JavaScript and YAML files, `.github` included, but not the
 *  build's output, the other hidden directories, or the untracked `paper`.
 */
private def repositoryTexts(): List[Path] =
  val repository = Corpus.root.getParent
  val texts = List(".scala", ".md", ".txt", ".sh", ".py", ".mjs", ".yml")
  val found = mutable.ListBuffer.empty[Path]
  Files.walkFileTree(
    repository,
    new SimpleFileVisitor[Path]:
      override def preVisitDirectory(dir: Path, attributes: BasicFileAttributes): FileVisitResult =
        val n = String.valueOf(dir.getFileName)
        if dir != repository && (n.startsWith(".") && n != ".github" || Set("target", "node_modules", "paper")(n)) then
          FileVisitResult.SKIP_SUBTREE
        else FileVisitResult.CONTINUE
      override def visitFile(file: Path, attributes: BasicFileAttributes): FileVisitResult =
        if texts.exists(file.toString.endsWith) then found += file
        FileVisitResult.CONTINUE
  )
  found.toList.filter(p => scala.util.Try(Files.readString(p)).isSuccess)
