package io.bovel.vik

import scala.collection.mutable.ArrayBuffer
import scala.util.boundary
import scala.util.boundary.break

import io.bovel.cdclt.Lit
import io.bovel.egraph.{App, EClassId, EConflict, EqProof}
import io.bovel.lia

/** Checks the lemmas of the UF theory against their certificates (see
 *  [[UfTheory.Certificate]]), reading the terms off the applications the
 *  certificate recorded for its classes, never the e-graph's union-find: each
 *  step of a proof is checked on its own, from the literals of the lemma.
 *
 *  `atoms(v)` is the UF atom `t = u` of variable `v`; `trueLit` is the literal
 *  of `True`, the reason of the disequality `true != false`; `constructors`
 *  maps the class of each constructor symbol to its selectors' classes,
 *  `testers` the class of each tester to its constructor's class.
 */
final class UfLemmas(
    atoms: ArrayBuffer[(EClassId, EClassId) | Null],
    trueClass: EClassId,
    falseClass: EClassId,
    trueLit: Lit,
    constructors: Map[EClassId, Seq[EClassId]],
    testers: Map[EClassId, EClassId]
):
  /** Whether `certificate` shows that `clause` holds: `None` if so, what is
   *  wrong otherwise.
   */
  def check(clause: Array[Lit], certified: UfTheory.Certified): Option[String] = boundary:
    val certificate = certified.certificate
    object eg:
      def app(x: EClassId): App | Null = certified.nodes.getOrElse(x, null)
    def fail(msg: String): Nothing = break(Some(msg))
    // The hypotheses: the negations of the clause's literals.
    val hyps = clause.iterator.map(_.neg.index).toSet
    def same(p: (EClassId, EClassId), q: (EClassId, EClassId)): Boolean = p == q || p == q.swap

    def asserted(a: EClassId, b: EClassId, reason: Int): Unit =
      if !hyps(reason) then fail(s"the reason $reason is not a hypothesis")
      val lit = Lit.fromIndex(reason)
      val atom = if lit.variable < atoms.length then atoms(lit.variable) else null
      if atom == null then fail(s"the reason $reason has no atom")
      if lit.positive then
        if !same((a, b), atom) then fail(s"$a = $b is not the atom of $reason")
      else if !(atom._2 == trueClass && same((a, b), (atom._1, falseClass))) then
        fail(s"$a = $b does not follow from the negation of $reason")

    /** The head of the class `x`'s term when it is a constructor: its class and
     *  its arguments.
     */
    def constructorOf(x: EClassId): Option[(EClassId, Seq[EClassId])] =
      if constructors.get(x).exists(_.isEmpty) then Some((x, Nil))
      else
        val app = eg.app(x)
        if app != null && constructors.contains(app.fun) then Some((app.fun, app.args)) else None

    /** Whether `a = b` follows from the premises by a rule of the datatypes. */
    def rule(a: EClassId, b: EClassId, premises: Seq[(EClassId, EClassId)]): Boolean =
      def oriented(a: EClassId, b: EClassId): Boolean =
        val app = eg.app(a)
        premises match
          case Seq((p, q)) =>
            // A selector of a constructor node: `s_i(t) = x_i` when `t = C(x_1, ..., x_n)`.
            val selector = app != null && app.args.length == 1 && app.args(0) == p &&
              constructorOf(q).exists((c, args) =>
                constructors(c).indexOf(app.fun) match
                  case -1 => false
                  case i => args(i) == b
              )
            // A tester of a constructor node: `is_C(t) = true` when `t = C(...)`.
            val tester = app != null && app.args.length == 1 && app.args(0) == p && b == trueClass &&
              testers.get(app.fun).exists(c => constructorOf(q).exists(_._1 == c))
            // Injectivity: `x_i = y_i` when `C(x_1, ...) = C(y_1, ...)`.
            val injective = (constructorOf(p), constructorOf(q)) match
              case (Some((c, xs)), Some((d, ys))) => c == d && xs.indices.exists(i => xs(i) == a && ys(i) == b)
              case _ => false
            // Expansion: `t = C(s_1(t), ..., s_n(t))` when `is_C(t) = true`.
            val expansion = q == trueClass && {
              val test = eg.app(p)
              test != null && test.args.length == 1 && test.args(0) == a && testers.get(test.fun).exists { c =>
                val selectors = constructors(c)
                if selectors.isEmpty then b == c
                else
                  constructorOf(b).exists((d, fields) =>
                    d == c && fields.indices.forall { i =>
                      val field = eg.app(fields(i))
                      field != null && field.fun == selectors(i) && field.args == Seq(a)
                    }
                  )
              }
            }
            selector || tester || injective || expansion
          case _ => false
      oriented(a, b) || oriented(b, a)

    /** Checks `p` and returns the equality it proves. */
    def proves(p: EqProof): (EClassId, EClassId) = p match
      case EqProof.Refl(a) => (a, a)
      case EqProof.Given(a, b, reason) =>
        asserted(a, b, reason)
        (a, b)
      case EqProof.Symm(q) => proves(q).swap
      case EqProof.Trans(q, r) =>
        val (a, b) = proves(q)
        val (c, d) = proves(r)
        if b != c then fail(s"transitivity through $b and $c")
        (a, d)
      case EqProof.Cong(a, b, fun, args) =>
        val (l, r) = (eg.app(a), eg.app(b))
        if l == null || r == null || l.args.length != args.length || r.args.length != args.length then
          fail(s"congruence of $a and $b, not applications of the same arity")
        if proves(fun) != (l.fun, r.fun) then fail(s"congruence of $a and $b: not their functions")
        for i <- args.indices do
          if proves(args(i)) != (l.args(i), r.args(i)) then fail(s"congruence of $a and $b: not their argument $i")
        (a, b)
      case EqProof.Rule(a, b, premises) =>
        val holding = premises.map(proves)
        if !rule(a, b, holding) then fail(s"$a = $b by no rule from ${holding.mkString(", ")}")
        (a, b)

    /** Checks that `reason`, a hypothesis, makes `a` and `b` distinct. */
    def apart(a: EClassId, b: EClassId, reason: Int): Unit =
      if !hyps(reason) then fail(s"the reason $reason is not a hypothesis")
      val lit = Lit.fromIndex(reason)
      if lit == trueLit then
        if !same((a, b), (trueClass, falseClass)) then fail(s"$a != $b is not true != false")
      else
        val atom = if lit.variable < atoms.length then atoms(lit.variable) else null
        if lit.positive || atom == null || !same((a, b), atom) then fail(s"$a != $b is not the negation of $reason")

    /** Checks that the equalities `holding` contradict the datatypes. */
    def contradiction(holding: Seq[(EClassId, EClassId)]): Unit =
      val clash = holding match
        // Two constructors.
        case Seq((p, q)) =>
          (constructorOf(p), constructorOf(q)) match
            case (Some((c, _)), Some((d, _))) => c != d
            case _ => false
        // A tester true of a term with another constructor.
        case Seq((test, t), (x, n)) if t == trueClass =>
          val app = eg.app(test)
          app != null && app.args == Seq(x) && testers.get(app.fun).exists(c => constructorOf(n).exists(_._1 != c))
        case _ => false
      // A cycle: each argument of a constructor node is the next node, the last's the first.
      val cycle = holding.nonEmpty && holding.indices.forall { i =>
        val (arg, _) = holding(i)
        val (_, node) = holding((i + holding.length - 1) % holding.length)
        constructorOf(node).exists(_._2.contains(arg))
      }
      if !clash && !cycle then fail(s"no contradiction in ${holding.mkString(", ")}")

    certificate match
      case UfTheory.Certificate.Conflict(EConflict.Disequal(p, reason)) =>
        val (a, b) = proves(p)
        apart(a, b, reason)
      case UfTheory.Certificate.Conflict(EConflict.Contradiction(proofs)) =>
        contradiction(proofs.map(proves))
      case UfTheory.Certificate.Equal(p) =>
        val l = clause(0)
        val atom = atoms(l.variable)
        if !l.positive || atom == null || !same(proves(p), atom) then fail("not a proof of the implied atom")
      case UfTheory.Certificate.Apart(left, right, reason) =>
        val l = clause(0)
        val atom = atoms(l.variable)
        val (t, s) = proves(left)
        val (u, s2) = proves(right)
        if l.positive || atom == null || (t, u) != atom then fail("not the sides of the implied atom")
        apart(s, s2, reason)
    None

/** Checks the lemmas of the LIA theory against their certificates (see
 *  `lia.Certificate`): each hypothesis must be the negation of a literal of the
 *  lemma, asserting the bound of that literal's atom (`atoms(v)`, see
 *  [[LiaTheory]]).
 */
final class LiaLemmas(solver: lia.Solver, atoms: ArrayBuffer[LiaTheory.Atom | Null]):
  private val checker = lia.Certificate.Checker(
    solver.form,
    solver.isInteger,
    h =>
      val lit = Lit.fromIndex(h.reason)
      val atom = if lit.variable < atoms.length then atoms(lit.variable) else null
      atom match
        case LiaTheory.Atom.Bound(x, c, strict) =>
          // `x ≤ c` (or `<`), false: `x > c` (or `≥`).
          val expected = (x, c, if lit.positive then strict else !strict, lit.positive)
          if (h.x, h.c, h.strict, h.upper) == expected then None else Some(s"not the bound of $atom")
        case LiaTheory.Atom.Equal(x, c) =>
          if lit.positive && h.x == x && h.c == c && !h.strict then None else Some(s"not a side of $atom")
        case null => Some("no atom")
  )

  /** The certificates' `Trusted` steps met so far. */
  def trusted: Int = checker.trusted

  /** Whether `certificate` refutes the negations of the literals of `clause`.
   */
  def check(clause: Array[Lit], certificate: lia.Certificate): Option[String] =
    val hyps = clause.iterator.map(_.neg.index).toSet
    checker.check(certificate, hyps)
