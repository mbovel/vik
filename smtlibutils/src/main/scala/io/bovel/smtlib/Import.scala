package io.bovel.smtlib

import java.nio.file.{Files, Path}

import scala.collection.mutable
import scala.jdk.CollectionConverters.*
import scala.util.Try

/** Imports SMT-LIB files into the corpus (see `corpus/README.md`):
 *  `sbt "smtlibutils/runMain io.bovel.smtlib.smt2import <origin> <source root> [files per family]"`.
 *  Origins: `cvc5` (`test/regress/cli`, answers in `; EXPECT:` lines, or the
 *  file's own statuses where it has none), `z3test` (`regressions/smt2`,
 *  answers in `.expected.out` files), `princess` (`testcases`, answers in each
 *  directory's `Answers` log), `smtinterpol` (`SMTInterpolTest/test`, statuses
 *  in the files), `submission` and `smt-lib`/`smt-lib-incremental` (statuses in
 *  the files), `cvc5-examples` (`examples/api/smtlib` of the cvc5 repository)
 *  and `z3guide` (`website/docs-smtlib` of the z3 guide's repository: every
 *  `z3` code block of its pages), the last two without answers — `statusstamp`
 *  supplies them — and the regression suites of other solvers: `opensmt`
 *  (`test/regression`, answers in `.expected.out` files), `yices2`
 *  (`tests/regress`, answers in `.gold` files), `bitwuzla` (`test/regress`,
 *  answers in `.expect` files or the file's statuses), `stp`
 *  (`tests/query-files`, answers in the `; CHECK:` lines of its lit tests) and
 *  `smt-switch` (`tests/smt2`, statuses in the files). A file of those that
 *  states a license of its own keeps it, and its header says so; one under the
 *  GPL is left out. Every candidate is parsed: only the files in the supported
 *  fragment are copied, into `<logic>/<origin>/<family>__<file>.smt2`, with a
 *  header naming the origin, the original path and the license, and a
 *  `(set-info :status ...)` before each `check-sat` when the answers are known.
 */
@main def smt2import(args: String*): Unit =
  // A default for `perFamily` is not optional to `@main`: the arguments are read here.
  val refused = args.indexOf("--refused") match
    case -1 => None
    case i => args.lift(i + 1).map(Path.of(_))
  args.filterNot(a => a == "--refused" || refused.exists(_.toString == a)) match
    case Seq(origin, root) => Import.run(origin, Path.of(root), 0, refused)
    case Seq(origin, root, perFamily) => Import.run(origin, Path.of(root), perFamily.toInt, refused)
    case _ => println("usage: smt2import <origin> <source root> [files per family] [--refused <directory>]")

object Import:
  private val licenses = Map(
    "cvc5" -> "modified BSD, LICENSES/cvc5.txt",
    "cvc5-examples" -> "modified BSD, LICENSES/cvc5.txt",
    "z3guide" -> "MIT, LICENSES/z3guide.txt",
    "z3test" -> "MIT, LICENSES/z3test.txt",
    "princess" -> "BSD, LICENSES/princess.txt",
    "smtinterpol" -> "LGPL-3.0, LICENSES/smtinterpol.txt",
    "opensmt" -> "MIT, LICENSES/opensmt.txt",
    "yices2" -> "BSD-3-Clause, LICENSES/yices2.txt",
    "bitwuzla" -> "MIT, LICENSES/bitwuzla.txt",
    "stp" -> "MIT, LICENSES/stp.txt",
    "smt-switch" -> "BSD-3-Clause, LICENSES/smt-switch.txt",
    "submission" -> "see the file's header",
    "smt-lib-incremental" -> "CC BY 4.0 (SMT-LIB release 2025, incremental, Zenodo 15493096)",
    "smt-lib" -> "CC BY 4.0 (SMT-LIB release 2025, non-incremental, Zenodo 16740866)"
  )
  private val maxSize = 200000L
  private val answers = Set("sat", "unsat", "unknown")

  /** A file to import: its text, the expected answers when they come from
   *  outside it, its family, and its file name when not the original's (a block
   *  cut out of a page).
   */
  final case class Candidate(
      rel: String,
      text: String,
      expected: Option[List[String]],
      family: String,
      name: String = ""
  )

  /** The directory of the corpus for a file: the smallest standard logic it
   *  fits ([[Logic.smallest]]), or, where z3 cannot tell, the logic it sets if
   *  standard, else a guess from what it declares.
   */
  def classify(text: String): String = Logic.smallest(text).getOrElse(fallback(text))

  private def fallback(text: String): String = Logic.claimed(text) match
    case List(name) if Logic.standard.contains(name) => name
    case _ =>
      if text.contains("declare-datatype") then "QF_DT"
      else if "\\(_ bv\\d+ \\d+\\)|\\(_ BitVec".r.findFirstIn(text).isDefined then "QF_BV"
      else if "\\bInt\\b".r.findFirstIn(text).isDefined then
        if "\\(declare-fun \\S+ \\([^)]".r.findFirstIn(text).isDefined || text.contains("declare-sort") then "QF_UFLIA"
        else "QF_LIA"
      else "QF_UF"

  /** The `z3` code blocks of a page of the z3 guide (not the `z3-duo`, `z3-py`,
   *  `z3-js` ones), each a script of its own.
   */
  private def guideBlocks(page: String): List[String] =
    "(?ms)^```z3(?: [^\n]*)?\n(.*?)^```".r.findAllMatchIn(page).map(_.group(1).nn).toList

  /** The answers among the lines of a file of expected output, if it holds no
   *  error: a suite's test of a refused script is no benchmark.
   */
  private def sidecar(path: Path): Option[List[String]] =
    Option.when(Files.exists(path))(Files.readAllLines(path).asScala.map(_.trim.nn).filter(_.nonEmpty).toList)
      .filter(lines => !lines.exists(l => l.startsWith("(error") || l.toLowerCase.nn.contains("error")))
      .map(_.filter(answers))
      .filter(_.nonEmpty)

  /** How many `check-sat`s a script runs, a commented one aside. */
  private def checks(text: String): Int =
    "(?m)^[^;\\n]*\\(check-sat".r.findAllIn(text).length

  /** The answers a lit test of STP expects: its `; CHECK:` and `; CHECK-NEXT:`
   *  patterns that are an answer (`^sat`, `unsat$`), when there is one per
   *  `check-sat`; none for a test that checks other output only.
   */
  private def litAnswers(text: String): Option[List[String]] =
    val found = "(?m)^\\s*;+\\s*CHECK(?:-NEXT)?(?:-L)?:\\s*\\^?(sat|unsat|unknown)\\$?\\s*$".r
      .findAllMatchIn(text).map(_.group(1).nn).toList
    Option.when(found.nonEmpty && found.length == checks(text))(found)

  /** The license a file states for itself, where it does: a benchmark that a
   *  suite took from elsewhere says so in its header, its `:license` or an SPDX
   *  line. `GPL` for any of the GPL family, which is left out.
   */
  def ownLicense(text: String): Option[String] =
    val head = text.linesIterator.filter(l => l.startsWith(";") || l.contains("set-info")).take(60).mkString("\n")
    val stated = "(?i)\\(set-info :license \"([^\"]+)\"\\)|SPDX-License-Identifier:\\s*(\\S[^\\n]*)".r
      .findFirstMatchIn(head).map(m => Option(m.group(1)).getOrElse(m.group(2)).nn.trim)
    val ccBy = "(?i)creativecommons\\.org/licenses/by/4|CC[- ]BY[- ]4".r
    if "(?i)\\bL?GPL|GNU (Lesser )?General Public".r.findFirstIn(head).isDefined then Some("GPL")
    else if stated.exists(l => ccBy.findFirstIn(l).isDefined && !l.contains(" AND ")) then Some("CC BY 4.0")
    else stated.orElse(Option.when(ccBy.findFirstIn(head).isDefined)("CC BY 4.0"))

  /** `02 - basiccommands.md` as `basiccommands`: the guide numbers its pages
   *  and sections.
   */
  private def guideSlug(file: String): String =
    file.stripSuffix(".md").replaceFirst("^\\d+ - ", "").replace(" ", "-").toLowerCase

  def candidates(origin: String, root: Path): List[Candidate] =
    val extension = if origin == "z3guide" then ".md" else ".smt2"
    val stream = Files.walk(root)
    val paths =
      try stream.iterator().asScala.filter(p => Files.isRegularFile(p) && p.toString.endsWith(extension)).toList.sorted
      finally stream.close()
    paths.flatMap { path =>
      val rel = root.relativize(path).toString
      val parts = rel.split("/").toList
      Try(Files.readString(path)).toOption.filter(_ => Files.size(path) <= maxSize).toList.flatMap { text =>
        if origin == "z3guide" then
          val family = guideSlug(parts.head)
          guideBlocks(text).zipWithIndex.collect {
            case (block, i) if block.contains("check-sat") =>
              Candidate(s"$rel (block ${i + 1})", block, None, family, s"${guideSlug(parts.last)}_${i + 1}.smt2")
          }
        else if !text.contains("check-sat") then None
        else
          origin match
            case "cvc5-examples" => Some(Candidate(rel, text, None, "examples"))
            case "cvc5" =>
              val options = "(?m)^; COMMAND-LINE: (.*)$".r.findAllMatchIn(text).flatMap(_.group(1).nn.split(" ")).toList
              val harmless = List(
                "--incremental",
                "--produce-models",
                "--produce-unsat-cores",
                "--lang",
                "-q",
                "--no-",
                "--check-",
                "--strings-exp",
                "--fmf",
                "--full-saturate",
                "--ee-mode",
                "--strict-parsing",
                "--model-u-print",
                "-o",
                "-i"
              )
              val expected =
                "(?m)^; EXPECT: (.*)$".r.findAllMatchIn(text).map(_.group(1).nn.trim).toList.filter(answers)
              if "(?m)^; (EXIT: [1-9]|EXPECT-ERROR|REQUIRES:)".r.findFirstIn(text).isDefined then None
              else if options.exists(o => o.startsWith("-") && !harmless.exists(o.startsWith)) then
                None // the answers may depend on it
              else if !text.contains("; EXPECT:") then
                // No `; EXPECT:` lines: cvc5's runner checks the `(set-info :status ...)` before each
                // check-sat, which the file keeps — read as `Script.expectedStatuses` reads it, the last
                // since the previous check-sat, so an old header's `unknown` does not count.
                Option.when(Try(Parser.parse(text).expectedStatuses.exists(_.isDefined)).getOrElse(false))(
                  Candidate(rel, text, None, if parts.length > 2 then parts(0) + "-" + parts(1) else parts(0))
                )
              else if expected.isEmpty || !expected.exists(answers) then None
              else if !expected.forall(answers) && !text.contains("get-value") && !text.contains("get-model") &&
                !text.contains("get-unsat-core")
              then None // a non-answer expectation with no value query: options or errors we do not model
              else
                Some(Candidate(
                  rel,
                  text,
                  Some(expected),
                  if parts.length > 2 then parts(0) + "-" + parts(1) else parts(0)
                ))
            case "z3test" =>
              val out = path.resolveSibling(path.getFileName.nn.toString.dropRight(5) + ".expected.out")
              Option.when(Files.exists(out))(
                Files.readAllLines(out).asScala.map(_.trim).filter(_.nonEmpty).toList.filter(answers)
              ).filter(_.nonEmpty)
                .map(e => Candidate(rel, text, Some(e), "regressions"))
            case "princess" =>
              val log = path.resolveSibling("Answers")
              Option.when(Files.exists(log))(Files.readString(log)).flatMap { answersLog =>
                val name = java.util.regex.Pattern.quote(path.getFileName.nn.toString)
                s"(?ms)^Loading $name \\.\\.\\.\\n(.*?)(?=^Loading |\\z)".r.findFirstMatchIn(
                  answersLog
                ).map(_.group(1).nn)
              }.map(_.linesIterator.map(_.trim).filter(answers).toList).filter(_.nonEmpty)
                .map(e => Candidate(rel, text, Some(e), parts(parts.length - 2)))
            case "opensmt" => // test/regression/<suite>/<family>/file.smt2 and file.smt2.expected.out
              val family = if parts.length > 2 then parts(0) + "-" + parts(1) else parts(0)
              val err = path.resolveSibling(path.getFileName.nn.toString + ".expected.err")
              Option.when(!Files.exists(err) || Files.size(err) == 0)(())
                .flatMap(_ => sidecar(path.resolveSibling(path.getFileName.nn.toString + ".expected.out")))
                .map(e => Candidate(rel, text, Some(e), family))
            case "yices2" => // tests/regress/<family>/[subdirs/]file.smt2 and file.smt2.gold
              // `coverage/smtlib` holds benchmarks of other origins, some under the GPL: left for a review of
              // their provenance (issue 222).
              val family = if parts.length > 1 then parts.init.mkString("-") else "root"
              Option.when(!rel.startsWith("coverage/smtlib/"))(())
                .flatMap(_ => sidecar(path.resolveSibling(path.getFileName.nn.toString + ".gold")))
                .map(e => Candidate(rel, text, Some(e), family))
            case "bitwuzla" => // test/regress/<family>/[subdirs/]file.smt2, with file.expect or its own statuses
              val family = if parts.length > 1 then parts.init.mkString("-") else "root"
              val expect = path.resolveSibling(path.getFileName.nn.toString.dropRight(5) + ".expect")
              if Files.exists(expect) then sidecar(expect).map(e => Candidate(rel, text, Some(e), family))
              else Some(Candidate(rel, text, None, family))
            case "stp" => // tests/query-files/<family>/[subdirs/]file.smt2, a lit test
              val family = if parts.length > 1 then parts.init.mkString("-") else "root"
              if "(?m)^\\s*;+\\s*(XFAIL|REQUIRES):".r.findFirstIn(text).isDefined then None
              else litAnswers(text).map(e => Candidate(rel, text, Some(e), family))
            case "smt-switch" => // tests/smt2/<family>/file.smt2, statuses in the files; `errors` are refusals
              Option.when(parts.head != "errors")(Candidate(rel, text, None, parts.head))
            case "smtinterpol" => // SMTInterpolTest/test/<family>/[subdirs/]file, statuses in the files
              Some(Candidate(rel, text, None, if parts.length > 1 then parts.init.mkString("__") else "root"))
            case "submission" => // <non-incremental|incremental>/<logic>/<family>/.../file
              val family =
                if parts.length > 3 then parts(2) else if parts.length > 1 then parts(parts.length - 2) else "root"
              Some(Candidate(rel, text, None, if parts.head == "incremental" then "incremental-" + family else family))
            case _ => // smt-lib, smt-lib-incremental: <logic>/<family>/[subdirs/]file
              val family = if parts.length > 2 then parts.slice(1, parts.length - 1).mkString("__")
              else if parts.length > 1 then parts(parts.length - 2) else "root"
              Some(Candidate(rel, text, None, family))
      }
    }

  /** `(set-info :status X)` before each `check-sat` (also `-assuming`,
   *  `-using`), `X` from `expected` in order.
   */
  def withStatuses(text: String, expected: List[String]): String =
    val checks = "\\(check-sat-assuming|\\(check-sat-using|\\(check-sat(?=[ )])".r
    // A check-sat inside a comment is no check-sat: a status spliced into `;(check-sat)` stayed in the
    // comment, made the command live, and shifted the answers of the real ones (issue 56).
    def commented(at: Int): Boolean = text.lastIndexOf(';', at) > text.lastIndexOf('\n', at)
    val out = StringBuilder()
    var last = 0
    var k = 0
    for m <- checks.findAllMatchIn(text) if !commented(m.start) do
      out ++= text.substring(last, m.start)
      out ++= s"(set-info :status ${expected.lift(k).getOrElse("unknown")})\n" + m.matched
      k += 1
      last = m.end
    out ++= text.substring(last)
    out.toString

  private val parallelism = 32
  private val loweringSeconds = 120L

  /** With `refused`, a candidate VIK refuses is written there as it would be to
   *  the corpus, in the directory of the logic it claims or seems to use, and
   *  the reason goes to `refused.tsv` beside it: what the corpus may want on
   *  `ignore.txt` until VIK reads it, chosen by hand.
   */
  def run(origin: String, root: Path, perFamily: Int, refused: Option[Path] = None): Unit =
    val found = candidates(origin, root)
    val chosen =
      if perFamily > 0 then found.groupBy(_.family).values.flatMap(_.sortBy(_.text.length).take(perFamily)).toList
      else found
    val rejected = mutable.Map.empty[String, Int]
    var copied = 0
    // The parser reads the whole language; the fragment line is the lowering's, so both must pass — on a
    // pool of threads, the lowering being the cost; a candidate whose lowering outlasts the limit is
    // rejected as such (its thread runs on, so a few of them cost parallelism, not the run).
    val pool = java.util.concurrent.Executors.newFixedThreadPool(
      parallelism,
      r =>
        val t = new Thread(r)
        t.setDaemon(true)
        t
    )
    // A regression suite holds benchmarks of other origins beside its own tests.
    val suite = Set("opensmt", "yices2", "bitwuzla", "stp", "smt-switch")(origin)
    def own(c: Candidate): Option[String] = if suite then ownLicense(c.text) else None
    val lowered = chosen.filter(c => !own(c).contains("GPL")).map { c =>
      val license = own(c) match
        case Some(stated) if !licenses(origin).startsWith(stated) =>
          s"$stated, as the file states; the suite is ${licenses(origin)}"
        case _ => licenses(origin)
      val content = s"; From $origin: ${c.rel} ($license)\n" +
        c.expected.fold(c.text)(withStatuses(c.text, _))
      // The file goes to the smallest logic it fits, and says so (`Logic.relabel`) where it set another.
      val outcome = pool.submit[Try[(String, String)]](() =>
        Try {
          io.bovel.vik.Lowering(Parser.parse(content))
          Logic.smallest(content) match
            case Some(logic) =>
              (logic, if Logic.claimed(content) == List(logic) then content else Logic.relabel(content, logic))
            case None => (fallback(content), content)
        }
      )
      (c, content, outcome)
    }
    for (c, content, outcome) <- lowered do
      Try(outcome.get(loweringSeconds, java.util.concurrent.TimeUnit.SECONDS)).flatten.recoverWith {
        case _: java.util.concurrent.TimeoutException =>
          scala.util.Failure(RuntimeException(s"lowering past $loweringSeconds s"))
      } match
        case scala.util.Success((logic, text)) =>
          val dir = Corpus.root.resolve(logic).resolve(origin)
          Files.createDirectories(dir)
          val file = if c.name.nonEmpty then c.name else Path.of(c.rel).getFileName.toString
          val name = s"${c.family}__$file".replace("/", "_")
          Files.writeString(dir.resolve(name), text)
          copied += 1
        case scala.util.Failure(e) =>
          val message = s"${e.getClass.getSimpleName}: ${e.getMessage}".take(60)
          rejected(message) = rejected.getOrElse(message, 0) + 1
          for out <- refused do
            val dir = out.resolve(fallback(content)).resolve(origin)
            Files.createDirectories(dir)
            val file = if c.name.nonEmpty then c.name else Path.of(c.rel).getFileName.toString
            val name = s"${c.family}__$file".replace("/", "_")
            Files.writeString(dir.resolve(name), content)
            val line = s"${out.relativize(dir.resolve(name))}\t${message.replace('\n', ' ')}\n"
            Files.writeString(
              out.resolve("refused.tsv"),
              line,
              java.nio.file.StandardOpenOption.CREATE,
              java.nio.file.StandardOpenOption.APPEND
            )
    println(s"$origin: ${chosen.length} candidates, $copied in the fragment, copied")
    for (message, n) <- rejected.toList.sortBy(-_._2).take(15) do println(f"  rejected $n%5d  $message")
