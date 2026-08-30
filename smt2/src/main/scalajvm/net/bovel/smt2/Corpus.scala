package net.bovel.smt2

import java.nio.file.{Files, Path}

import scala.jdk.CollectionConverters.*

/** The SMT-LIB files of the repository's `smtlib` directory — by logic, then
 *  origin (see `smtlib/README.md`) — for the tests and the benchmarks, and
 *  `smtlib/status.tsv`, the recorded outcome of the solver on each.
 */
object Corpus:
  /** The `smtlib` directory: up from this source file to the repository root.
   */
  val root: Path =
    Iterator.iterate(Path.of(SourcePath.here).toAbsolutePath)(_.getParent)
      .takeWhile(_ != null)
      .map(_.resolve("smtlib"))
      .find(Files.isDirectory(_))
      .getOrElse(throw IllegalStateException("no smtlib directory above " + SourcePath.here))

  /** Every `.smt2` file, by path relative to `root`, sorted. */
  def files: List[String] =
    val stream = Files.walk(root)
    try
      stream.iterator().asScala
        .filter(p => Files.isRegularFile(p) && p.toString.endsWith(".smt2") && !p.toString.contains("/.import/"))
        .map(p => root.relativize(p).toString)
        .toList
        .sorted
    finally stream.close()

  /** The file named `name` (unique across the corpus: the family prefixes its
   *  files).
   */
  def find(name: String): Path =
    files.find(f => Path.of(f).getFileName.toString == name).map(root.resolve).getOrElse(
      throw java.util.NoSuchElementException(s"no $name in $root")
    )

  def read(relative: String): String = Files.readString(root.resolve(relative))

  /** The logic of a file: the first directory of its relative path. */
  def logic(relative: String): String = Path.of(relative).getName(0).toString

  /** The recorded outcomes, `status.tsv`: relative path, then the solver's
   *  answers (`sat`/`unsat` per `check-sat`, joined by `,`), or `timeout`,
   *  `wrong` or `unsupported`, then the seconds taken.
   */
  def status: Map[String, (String, Double)] =
    val file = root.resolve("status.tsv")
    if !Files.exists(file) then Map.empty
    else
      Files.readAllLines(file).asScala.toList.drop(1).filter(_.nonEmpty).map { line =>
        val Array(path, outcome, seconds) = line.split("\t", 3)
        path -> (outcome, seconds.toDouble)
      }.toMap
