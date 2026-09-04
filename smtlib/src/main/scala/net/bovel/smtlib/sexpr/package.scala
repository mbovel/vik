package net.bovel.smtlib

/** Reading the text: [[sexpr.SExprParser.parse]] turns a string into
 *  [[sexpr.SExpr]]s, the standard's own category (SMT-LIB 2.6, 3.2 — every
 *  syntactic category is a specialization of `⟨s_expr⟩`), in one pass and with
 *  no token list materialised. [[Parser]] reads the script above it.
 */
package object sexpr
