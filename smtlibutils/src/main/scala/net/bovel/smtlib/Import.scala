package net.bovel.smtlib

import java.nio.file.{Files, Path}

import scala.collection.mutable
import scala.jdk.CollectionConverters.*
import scala.util.Try

/** Imports SMT-LIB files into the corpus (see `corpus/README.md`):
 *  `sbt "smtlibutils/runMain net.bovel.smtlib.smt2import <origin> <source root> [files per family]"`.
 *  Origins: `cvc5` (`test/regress/cli`, answers in `; EXPECT:` lines), `z3test`
 *  (`regressions/smt2`, answers in `.expected.out` files), `princess`
 *  (`testcases`, answers in each directory's `Answers` log), `submission` and
 *  `smt-lib`/`smt-lib-incremental` (statuses in the files). Every candidate is
 *  parsed: only the files in the supported fragment are copied, into
 *  `<logic>/<origin>/<family>__<file>.smt2`, with a header naming the origin,
 *  the original path and the license, and a `(set-info :status ...)` before
 *  each `check-sat`.
 */
@main def smt2import(origin: String, root: String, perFamily: Int = 0): Unit =
  Import.run(origin, Path.of(root), perFamily)

object Import:
  private val licenses = Map(
    "cvc5" -> "modified BSD, LICENSES/cvc5.txt",
    "z3test" -> "MIT, LICENSES/z3test.txt",
    "princess" -> "BSD, LICENSES/princess.txt",
    "submission" -> "see the file's header",
    "smt-lib-incremental" -> "CC BY 4.0 (SMT-LIB release 2025, incremental, Zenodo 15493096)",
    "smt-lib" -> "CC BY 4.0 (SMT-LIB release 2025, non-incremental, Zenodo 16740866)"
  )
  private val maxSize = 200000L
  private val answers = Set("sat", "unsat", "unknown")

  /** A file to import: its text, the expected answers when they come from
   *  outside it, and its family.
   */
  final case class Candidate(rel: String, text: String, expected: Option[List[String]], family: String)

  /** The directory of the corpus for a file: its `set-logic`, or what it uses
   *  when that is `ALL`.
   */
  def classify(text: String): String =
    val logic = "\\(set-logic ([A-Za-z_]+)\\)".r.findFirstMatchIn(text).map(_.group(1).nn).getOrElse("ALL")
    logic match
      case "QF_UF" | "QF_LIA" | "QF_UFLIA" | "QF_DT" | "QF_BV" | "QF_LRA" | "QF_UFLRA" | "QF_AX" | "QF_ALIA" | "QF_AUFLIA" | "QF_LIRA" => logic
      case "QF_UFLIRA" => "QF_LIRA"
      case "QF_UFDT" | "QF_UFDTLIA" | "QF_DTLIA" => "QF_DT"
      case "QF_UFBV" | "BV" => "QF_BV"
      case _ if logic.startsWith("HO_") || text.contains("(lambda ") || text.contains("(-> ") => "HO"
      case _ if text.contains("declare-datatype") => "QF_DT"
      case _ if "\\(_ bv\\d+ \\d+\\)|\\(_ BitVec".r.findFirstIn(text).isDefined => "QF_BV"
      case _ if "\\bInt\\b".r.findFirstIn(text).isDefined =>
        if "\\(declare-fun \\S+ \\([^)]".r.findFirstIn(text).isDefined || text.contains("declare-sort") then "QF_UFLIA"
        else "QF_LIA"
      case _ => "QF_UF"

  def candidates(origin: String, root: Path): List[Candidate] =
    val stream = Files.walk(root)
    val paths =
      try stream.iterator().asScala.filter(p => Files.isRegularFile(p) && p.toString.endsWith(".smt2")).toList.sorted
      finally stream.close()
    paths.flatMap { path =>
      val rel = root.relativize(path).toString
      val parts = rel.split("/").toList
      Try(Files.readString(path)).toOption.filter(_ => Files.size(path) <= maxSize).flatMap { text =>
        if !text.contains("check-sat") then None
        else
          origin match
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
                "--full-saturate"
              )
              val expected =
                "(?m)^; EXPECT: (.*)$".r.findAllMatchIn(text).map(_.group(1).nn.trim).toList.filter(answers)
              if "(?m)^; (EXIT: [1-9]|EXPECT-ERROR|REQUIRES:)".r.findFirstIn(text).isDefined then None
              else if options.exists(o => o.startsWith("-") && !harmless.exists(o.startsWith)) then
                None // the answers may depend on it
              else if expected.isEmpty || !expected.exists(answers) then None
              else if !expected.forall(answers) && !text.contains("get-value") && !text.contains("get-model") && !text.contains("get-unsat-core")
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
    val out = StringBuilder()
    var last = 0
    var k = 0
    for m <- checks.findAllMatchIn(text) do
      out ++= text.substring(last, m.start)
      out ++= s"(set-info :status ${expected.lift(k).getOrElse("unknown")})\n" + m.matched
      k += 1
      last = m.end
    out ++= text.substring(last)
    out.toString

  def run(origin: String, root: Path, perFamily: Int): Unit =
    val found = candidates(origin, root)
    val chosen =
      if perFamily > 0 then found.groupBy(_.family).values.flatMap(_.sortBy(_.text.length).take(perFamily)).toList
      else found
    val rejected = mutable.Map.empty[String, Int]
    var copied = 0
    for c <- chosen do
      val content = s"; From $origin: ${c.rel} (${licenses(origin)})\n" +
        c.expected.fold(c.text)(withStatuses(c.text, _))
      // The parser reads the whole language; the fragment line is the lowering's, so both must pass.
      Try(net.bovel.vik.Lowering(Parser.parse(content))) match
        case scala.util.Success(_) =>
          val dir = Corpus.root.resolve(classify(c.text)).resolve(origin)
          Files.createDirectories(dir)
          val name = s"${c.family}__${Path.of(c.rel).getFileName.nn}".replace("/", "_")
          Files.writeString(dir.resolve(name), content)
          copied += 1
        case scala.util.Failure(e) =>
          val message = s"${e.getClass.getSimpleName}: ${e.getMessage}".take(60)
          rejected(message) = rejected.getOrElse(message, 0) + 1
    println(s"$origin: ${chosen.length} candidates, $copied in the fragment, copied")
    for (message, n) <- rejected.toList.sortBy(-_._2).take(15) do println(f"  rejected $n%5d  $message")
