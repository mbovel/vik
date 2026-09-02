package net.bovel.smtlib

import java.nio.file.{Files, Path}

import scala.jdk.CollectionConverters.*

/** The SMT-LIB files of the repository's `corpus` directory — by logic, then
 *  origin (see `corpus/README.md`) — for the tests and the benchmarks, and
 *  `corpus/ignore.txt`, the files the tests expect to fail.
 */
object Corpus:
  /** The `corpus` directory: up from this source file to the repository root.
   */
  val root: Path =
    Iterator.iterate(Path.of(SourcePath.here).toAbsolutePath)(_.getParent)
      .takeWhile(_ != null)
      .map(_.resolve("corpus"))
      .find(Files.isDirectory(_))
      .getOrElse(throw IllegalStateException("no corpus directory above " + SourcePath.here))

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

  /** `ignore.txt`: the files the solver is known not to answer within the
   *  tests' budget, or to answer wrongly, with the reason; the tests expect
   *  these to fail, and a fixed one to be removed from the list.
   */
  def ignored: Map[String, String] =
    val file = root.resolve("ignore.txt")
    if !Files.exists(file) then Map.empty
    else
      Files.readAllLines(file).asScala.toList.filter(l => l.nonEmpty && !l.startsWith("#")).map { line =>
        val Array(path, reason) = line.split("\t", 2)
        path -> reason
      }.toMap
