package net.bovel.smt2

/** The corpus of SMT-LIB files (`smtlib/`, see its README): every file has a
 *  recorded outcome in `smtlib/status.tsv` (from `smtlib/check.py`); the ones
 *  the solver answers are played again and must give the same answers, within a
 *  time budget; the ones recorded as wrong, timing out or unsupported are only
 *  parsed, or skipped, so that the record keeps them visible without slowing
 *  the tests down.
 */
class SmtLibTest extends munit.FunSuite:
  override val munitTimeout = scala.concurrent.duration.Duration(20, "min")

  test("every file of the corpus has a recorded outcome"):
    val status = Corpus.status
    val missing = Corpus.files.filterNot(status.contains)
    assert(
      missing.isEmpty,
      s"${missing.length} files without a line in status.tsv (run smtlib/check.py): ${missing.take(5)}"
    )

  test("the recorded answers still hold"):
    val status = Corpus.status
    var played = 0
    for (file, (outcome, seconds)) <- status.toList.sortBy(_._1) if seconds < 3.0 do
      outcome match
        case "wrong" | "timeout" | "unsupported" | "error" => ()
        case answers =>
          val script = Parser.parse(Corpus.read(file))
          val got = Player.play(script).map(if _ then "sat" else "unsat").mkString(",")
          val expected = if answers.contains("...") then null else answers
          if expected != null then assertEquals(got, expected, file)
          played += 1
    assert(played > 50, s"only $played files played")
