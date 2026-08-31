package net.bovel.smt2

import scala.quoted.*

/** The path of the source file a call is written in (a macro, after Li Haoyi's
 *  sourcecode): to find the repository's files from tests and benchmarks,
 *  wherever they run.
 */
object SourcePath:
  inline def here: String = ${ hereImpl }

  private def hereImpl(using Quotes): Expr[String] =
    import quotes.reflect.*
    Expr(Position.ofMacroExpansion.sourceFile.path)
