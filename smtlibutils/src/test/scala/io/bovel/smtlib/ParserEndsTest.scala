package io.bovel.smtlib

import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration

/** The scripts the parser once did not come back from. Each is read in a thread
 *  of its own under a wall-clock limit, so that a regression fails here instead
 *  of hanging the run: that is why these tests are not in `ParserTest`, which
 *  also runs on Scala.js and Scala Native, where nothing can stop a loop.
 */
class ParserEndsTest extends munit.FunSuite:
  private def parse(text: String): Script = Await.result(Future(Parser.parse(text)), Duration(10, "s"))

  /** The selectors' sorts of the datatype `name`, as declared. */
  private def fields(script: Script, name: String): List[Sort] =
    script.commands.collect { case Command.DeclareDatatypes(datatypes) => datatypes }.flatten
      .find(_.name == name).get.constructors.flatMap(_.selectors.map(_._2))

  test("a sort parameter may be named like a sort of the script"):
    // The parameter `T` bound to the sort `T` was read as the parameter again, without end (issue 152).
    val list = "(declare-datatypes ((Lst 1)) ((par (T) ((nil) (cons (head T) (tail (Lst T)))))))"
    val after = parse(s"$list (declare-sort T 0) (declare-const a (Lst T)) (assert ((_ is cons) a))")
    assertEquals(fields(after, "(Lst T)"), List(Sort.Named("T"), Sort.Named("(Lst T)")))
    // In the declaration `T` is the parameter, also when the sort was there first: the parameter shadows it.
    val before = parse(s"""
      (declare-sort T 0) $list
      (declare-const a (Lst T)) (declare-const b (Lst Int)) (declare-const t T)
      (assert (= a (cons t (as nil (Lst T))))) (assert (= (head b) 1))""")
    assertEquals(fields(before, "(Lst T)"), List(Sort.Named("T"), Sort.Named("(Lst T)")))
    assertEquals(fields(before, "(Lst Int)"), List(Sort.Int, Sort.Named("(Lst Int)")))
    // The same for a declared sort parameter, at the instance of a function.
    val function = parse(
      "(declare-sort X 0) (declare-const a X) (declare-sort-parameter X) (declare-fun f (X) X) (assert (= (f a) a))"
    )
    assert(function.commands.contains(Command.DeclareFun("f@X", List(Sort.Named("X")), Sort.Named("X"))))
