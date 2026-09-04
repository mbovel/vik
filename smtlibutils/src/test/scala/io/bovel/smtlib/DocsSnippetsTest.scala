package io.bovel.smtlib

import java.nio.file.{Files, Path}

import scala.jdk.CollectionConverters.*

import io.bovel.vik.{Model, Session, Solver}

/** The SMT-LIB snippets of the documentation site (`docs/_docs`), which the
 *  pages let the reader run: each one is parsed and solved here, with the fuel
 *  the site gives it, so a snippet that stops working — a refused construct, an
 *  answer that does not come — fails the build instead of the page.
 */
class DocsSnippetsTest extends munit.FunSuite:
  private val fuel = 20000000L
  private val pages = Path.of(Corpus.root.getParent.toString, "docs", "_docs")

  private def markdown: List[Path] =
    val stream = Files.walk(pages)
    try stream.iterator().asScala.filter(_.toString.endsWith(".md")).toList.sorted
    finally stream.close()

  /** The bodies of the ```smt2 blocks of a page. */
  private def snippets(page: Path): List[String] =
    "(?s)```smt2\n(.*?)```".r.findAllMatchIn(Files.readString(page)).map(_.group(1).nn).toList

  for page <- markdown; (snippet, i) <- snippets(page).zipWithIndex do
    test(s"${pages.relativize(page)} snippet ${i + 1}"):
      val answers = Session.answers(Parser.parse(snippet), fuel = fuel)
      assert(answers.nonEmpty, "no check-sat")
      // A snippet that sets `:rlimit` is showing what a budget does, and answers `unknown` on purpose.
      if !snippet.contains(":rlimit") then
        assert(answers.forall(_.isDefined), s"unknown: out of fuel\n$snippet")
