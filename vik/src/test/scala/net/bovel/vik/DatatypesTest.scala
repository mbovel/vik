package net.bovel.vik

import net.bovel.smtlib.*
import net.bovel.smtlib.Formula.*
import net.bovel.smtlib.Term.*

/** Datatype declarations: parsed, represented, printed; refused at `check-sat`
 *  until the theory exists.
 */
class DatatypesTest extends munit.FunSuite:
  val list =
    Datatype(
      "List",
      List(Constructor("nil", Nil), Constructor("cons", List(("head", Sort.Int), ("tail", Sort.Named("List")))))
    )

  test("declare-datatype and declare-datatypes"):
    val single = Parser.parse("(declare-datatype List ((nil) (cons (head Int) (tail List))))")
    val plural = Parser.parse("(declare-datatypes ((List 0)) (((nil) (cons (head Int) (tail List)))))")
    assertEquals(single.commands, List(Command.DeclareDatatypes(List(list))))
    assertEquals(plural.commands, single.commands)
    intercept[Smt2Error](
      Parser.parse("(declare-datatypes ((List 1)) ((par (T) ((nil) (cons (head T) (tail (List T)))))))")
    )

  test("constructors, selectors and testers as terms"):
    val script = Parser.parse("""
      (declare-datatype List ((nil) (cons (head Int) (tail List))))
      (declare-const l List)
      (assert (= l (cons 1 nil)))
      (assert (= (head l) 1))
      (assert ((_ is cons) l))
      (assert (not ((_ is nil) (tail l))))""")
    val asserts = script.commands.collect { case Command.Assert(f) => f }
    val l = Const("l")
    assertEquals(asserts(0), Eq(List(l, App(Const("cons"), List(Numeral(1), Const("nil"))))))
    assertEquals(asserts(1), Eq(List(App(Const("head"), List(l)), Numeral(1))))
    assertEquals(asserts(2), IsConstructor("cons", l))
    assertEquals(asserts(3), Not(IsConstructor("nil", App(Const("tail"), List(l)))))
    assertEquals(Parser.parse(Printer.print(script)), script)
    assert(Printer.print(script).contains("(declare-datatypes ((List 0)) (((nil) (cons (head Int) (tail List)))))"))
    assert(Printer.print(script).contains("((_ is cons) l)"))

  test("scripts with datatypes are decided"):
    val decls =
      "(declare-sort U 0) (declare-datatype List ((nil) (cons (head U) (tail List)))) (declare-const l List) (declare-const m List) (declare-const a U) (declare-const b U)"
    def play(script: String) = Player.play(Parser.parse(s"$decls $script (check-sat)"))
    assertEquals(play("(assert (= l (cons a m))) (assert ((_ is nil) l))"), List(false))
    assertEquals(play("(assert (= l (cons a m))) (assert (= (head l) b)) (assert (distinct a b))"), List(false))
    assertEquals(play("(assert (= l (cons a m))) (assert (= (tail l) l))"), List(false))
    assertEquals(play("(assert (= l (cons a m))) (assert ((_ is cons) l)) (assert (= (head l) a))"), List(true))
    assertEquals(play("(assert (not ((_ is nil) l))) (assert (not ((_ is cons) l)))"), List(false))
    // Int fields: the datatype rules and the LIA solver combined
    assertEquals(
      Player.play(Parser.parse(
        "(declare-datatype L ((nil) (cons (head Int) (tail L)))) (declare-const l L) (assert (= l (cons 3 nil))) (assert (< (head l) 2)) (check-sat)"
      )),
      List(false)
    )
