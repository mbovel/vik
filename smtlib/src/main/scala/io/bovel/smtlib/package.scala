package io.bovel

/** Reading, playing and printing [SMT-LIB
 *  2](https://smt-lib.org/language.shtml) scripts:
 *  [[smtlib.sexpr.SExprParser.parse]] reads the text as [[smtlib.sexpr.SExpr]]s
 *  — the standard's own category, of which every other is a specialization —
 *  [[smtlib.Parser.parse]] turns those into a [[smtlib.Script]] of
 *  [[smtlib.Command]]s, the solver's [[vik.Session]] runs one, and
 *  [[smtlib.Printer]] writes it back.
 */
package object smtlib
