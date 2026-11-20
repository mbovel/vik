package io.bovel.cdclt

import scala.collection.mutable.ArrayBuffer

/** A theory solver plugged into [[Solver]] (CDCL(T), after Nieuwenhuis,
 *  Oliveras and Tinelli 2006, with the online interface of Z3's `theory` and
 *  cvc5's `Theory`): the solver asserts literals in trail order, asks the
 *  theory to check and propagate, opens a level at each decision and pops
 *  levels when backjumping. Literals of variables that are not atoms of the
 *  theory are asserted too; the theory ignores them.
 */
trait Theory:
  /** Asserts `lit` (a decision, or a propagation of the solver or of a theory).
   */
  def assertLit(lit: Lit): Unit

  /** Checks the literals asserted so far: `Conflict(reasons)` with asserted
   *  literals that are inconsistent together, or `Ok(implied)` with literals
   *  the asserted ones imply (their explanation is asked for lazily by
   *  [[explain]]). Called after every round of boolean propagation.
   */
  def propagate(): Theory.Result

  /** Like [[propagate]], called when every variable is assigned: a theory that
   *  only checks partially in `propagate` must decide here.
   */
  def finalCheck(): Theory.Result = propagate()

  /** The value the theory would give the atom of `v` now, when it has one — an
   *  arithmetic bound as the current relaxation stands to it — so that a
   *  decision on it moves nothing; `None` leaves the solver its saved phase.
   */
  def phase(v: Var): Option[Boolean] = None

  /** When the search leaves variables unassigned: a variable it has to decide
   *  for the theories' model to exist, [[Theory.nothingNeeded]] if there is a
   *  model, [[Theory.everythingNeeded]] if this cannot be told. Asked after a
   *  [[finalCheck]] that found nothing.
   */
  def neededDecision(): Var = Theory.nothingNeeded

  /** The asserted literals that implied `lit`, a literal returned by
   *  [[propagate]] or [[finalCheck]]; they were asserted before it.
   */
  def explain(lit: Lit): ArrayBuffer[Lit]

  /** Opens a level: [[pop]] undoes the assertions done after this call. */
  def push(): Unit

  def pop(): Unit

object Theory:
  enum Result:
    case Ok(implied: ArrayBuffer[Lit])
    case Conflict(reasons: ArrayBuffer[Lit])

    /** The theory gave up (its time budget is over): the search stops with
     *  `Unknown`.
     */
    case Unknown

    /** Splitting on demand (Barrett, Nieuwenhuis, Oliveras and Tinelli 2006):
     *  the theory needs a case split the solver has no atom for. `lit` is an
     *  unassigned literal, possibly of a variable the solver does not have yet
     *  (it adds the variables up to it); the solver decides it and goes on.
     *  From [[Theory.finalCheck]] only.
     */
    case Split(lit: Lit)

  val ok: Result = Result.Ok(ArrayBuffer.empty)
  val nothingNeeded: Var = -1
  val everythingNeeded: Var = -2

/** The theory with no atoms: [[Solver]] is then a SAT solver. */
object NoTheory extends Theory:
  def assertLit(lit: Lit): Unit = ()
  def propagate(): Theory.Result = Theory.ok
  def explain(lit: Lit): ArrayBuffer[Lit] = throw IllegalArgumentException("NoTheory implies nothing")
  def push(): Unit = ()
  def pop(): Unit = ()
