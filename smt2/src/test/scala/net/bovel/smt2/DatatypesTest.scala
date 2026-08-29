package net.bovel.smt2

import net.bovel.smt.{Constructor, Datatype, Formula, Term}
import net.bovel.smt.Formula.*
import net.bovel.smt.Term.*

/** Datatype declarations: parsed, represented, printed; refused at `check-sat`
 *  until the theory exists.
 */
class DatatypesTest extends munit.FunSuite:
  val list =
    Datatype("List", List(Constructor("nil", Nil), Constructor("cons", List(("head", "Int"), ("tail", "List")))))

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
    assertEquals(asserts(0), Eq(l, App(Const("cons"), List(Num(1), Const("nil")))))
    assertEquals(asserts(1), IntEq(App(Const("head"), List(l)), Num(1)))
    assertEquals(asserts(2), Pred(App(Const("(_ is cons)"), List(l))))
    assertEquals(asserts(3), Not(Pred(App(Const("(_ is nil)"), List(App(Const("tail"), List(l)))))))
    assertEquals(Parser.parse(Printer.print(script)), script)
    assert(Printer.print(script).contains("(declare-datatypes ((List 0)) (((nil) (cons (head Int) (tail List)))))"))
    assert(Printer.print(script).contains("((_ is cons) l)"))

  test("check-sat is refused until the theory exists"):
    val script = Parser.parse(
      "(declare-datatype List ((nil) (cons (head Int) (tail List)))) (declare-const l List) (assert (= l nil)) (check-sat)"
    )
    intercept[UnsupportedOperationException](Player.play(script))
