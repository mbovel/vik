package net.bovel

/** Reading, playing and printing [SMT-LIB
 *  2](https://smt-lib.org/language.shtml) scripts: [[smt2.Parser.parse]] turns
 *  text into a [[smt2.Script]] of [[smt2.Command]]s, the solver's
 *  [[vik.Player]] runs one, and [[smt2.Printer]] writes it back.
 */
package object smt2
