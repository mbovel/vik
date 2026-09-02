package net.bovel

/** Reading, playing and printing [SMT-LIB
 *  2](https://smt-lib.org/language.shtml) scripts: [[smtlib.Parser.parse]]
 *  turns text into a [[smtlib.Script]] of [[smtlib.Command]]s, the solver's
 *  [[vik.Session]] runs one, and [[smtlib.Printer]] writes it back.
 */
package object smtlib
