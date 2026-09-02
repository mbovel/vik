package net.bovel.vik
import net.bovel.smtlib as s2

/** Runs a whole script in a [[Session]] of its own and returns the responses,
 *  or just the answers of its `check-sat`s. A surface script
 *  (`net.bovel.smtlib`) is [[Lowering lowered]] first; a lowered [[Script]]
 *  plays as it is.
 */
object Player:
  private[vik] def valueText(m: Model, item: Formula | Term): String = item match
    case f: Formula => if m.holds(f) then "true" else "false"
    case t: Term => Printer.print(m.value(t))

  /** The model as SMT-LIB text: a define-fun per declared constant and per
   *  declared function (an ite chain over its graph); the interpreted symbols
   *  of the datatypes stay out.
   */
  private[vik] def modelText(m: Model, solver: Solver): String =
    val sorts = solver.declarations.collect { case (n, (Nil, s)) => n -> s }
    val interpreted =
      solver.datatypes.flatMap(_.constructors).flatMap(c => c.name :: c.tester :: c.selectors.map(_._1)).toSet
    val constants = m.constants.toList.filterNot((n, _) => interpreted(n)).sortBy(_._1).map((n, v) =>
      s"(define-fun $n () ${sorts.getOrElse(n, "?")} ${Printer.print(v)})"
    )
    val functions = m.functions.toList.filterNot((n, _) => interpreted(n)).sortBy(_._1).map { (n, f) =>
      val params = f.parameters.zipWithIndex.map((s, i) => s"(x!$i $s)").mkString(" ")
      val body = f.rows.foldRight(Printer.print(f.default)) { case ((args, r), els) =>
        val cond = args.zipWithIndex.map((a, i) => s"(= x!$i ${Printer.print(a)})") match
          case single :: Nil => single
          case cs => cs.mkString("(and ", " ", ")")
        s"(ite $cond ${Printer.print(r)} $els)"
      }
      s"(define-fun $n ($params) ${f.result} $body)"
    }
    (constants ++ functions).mkString("(", " ", ")")
  def run(script: s2.Script, solver: Solver = Solver(), fuel: Long = Long.MaxValue): List[String] =
    Session(solver, fuel).run(script)
  def run(lowered: Script, solver: Solver, fuel: Long): List[String] = Session(solver, fuel).play(lowered.commands)

  /** The answer of each `check-sat`: `Some(true)` for sat, `None` for unknown
   *  (the `fuel` of each `check-sat` is spent, see `Solver.solve`).
   */
  def answers(script: s2.Script, solver: Solver = Solver(), fuel: Long = Long.MaxValue): List[Option[Boolean]] =
    answersIn(run(script, solver, fuel))
  def answers(lowered: Script, solver: Solver, fuel: Long): List[Option[Boolean]] =
    answersIn(run(lowered, solver, fuel))

  /** The answers when unknown cannot happen (no budget). */
  def play(script: s2.Script, solver: Solver = Solver()): List[Boolean] = answers(script, solver).map(_.get)
  def play(lowered: Script, solver: Solver): List[Boolean] = answers(lowered, solver, Long.MaxValue).map(_.get)

  private def answersIn(responses: List[String]): List[Option[Boolean]] = responses.collect {
    case "sat" => Some(true)
    case "unsat" => Some(false)
    case "unknown" => None
  }
