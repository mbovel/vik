package io.bovel.vik

import scala.collection.mutable.{ArrayBuffer, HashMap, LinkedHashMap}

import io.bovel.cdclt
import io.bovel.cdclt.Lit
import io.bovel.egraph.{App, EClassId, EConflict, EqProof}
import io.bovel.lia
import io.bovel.smtlib.{Datatype, Sorts}

/** Writes the proof of an `Unsat` as Lean terms that `proofs/lean` checks: the
 *  lowered problem (`Vik.Low.Problem`), the answer (`Vik.Low.Answer`) and the
 *  theorem that `Problem.accepts` accepts it, by `decide +kernel`.
 *
 *  Terms and formulas become one table of nodes, each after its children,
 *  shared when equal. An e-class is the node of its term: a leaf the node of
 *  the term the encoder made it for, an application that of its function
 *  applied to its arguments. An LIA unknown is the node of its term, and a
 *  variable the expression it stands for over them.
 *
 *  With the script the check saw ([[Surface]]), it also writes the script
 *  (`Vik.Lower.SProblem`), the trace of its lowering (`Vik.Lower.Trace`), and
 *  the theorem that no model of the script satisfies its assertions. The table
 *  then starts with the script's sorts and symbols, then the minted ones, as
 *  `Trace.check` requires.
 */
private[vik] object LeanExport:

  /** What the encoder knows of a proof. */
  final case class Input(
      declarations: collection.Map[String, (List[String], String)],
      introduced: collection.Map[String, String],
      sortOf: Term => String,
      classTerms: collection.Map[EClassId, Term],
      trueClass: EClassId,
      falseClass: EClassId,
      atoms: collection.IndexedSeq[(EClassId, EClassId) | Null],
      liaAtoms: collection.IndexedSeq[LiaTheory.Atom | Null],
      meanings: collection.IndexedSeq[Formula | Null],
      trueLit: Lit,
      unknowns: collection.Map[lia.Var, Term],
      liaVars: Int,
      liaForm: lia.Var => Map[lia.Var, Rational],
      proof: cdclt.Proof,
      datatypes: List[Datatype],
      inputs: List[Formula],
      levels: List[(cdclt.Var, List[Formula])],
      assumed: List[cdclt.Var]
  )

  private type Rational = lia.Rational

  /** Not supported yet by the export. */
  final class Unsupported(msg: String) extends Exception(msg)

  /** The surface script at the check, from [[LoweringCheck.surface]]: its sorts
   *  (each with its key among the lowered sorts, as the encoder names them, and
   *  its lowering), its symbols with their sorts, its nodes with their sorts
   *  and lowerings, and what the lowering minted. Written as the `SProblem` and
   *  the `Trace` that `Trace.check` checks against the lowered problem.
   */
  final case class Surface(
      sorts: Seq[SortEntry],
      decls: Seq[(String, Int)],
      mints: Seq[(String, Mint)],
      nodes: Seq[String],
      sortOf: Seq[Int],
      values: Seq[Term | Formula],
      asserts: Seq[Int],
      datatypes: Seq[(Int, Seq[(Int, Int, Seq[Int])])],
      defs: Seq[String],
      insts: Seq[(Int, Seq[Int])],
      reasons: collection.Map[Formula, Reason]
  )

  /** A surface sort: its Lean term, its key among the lowered sorts, the Lean
   *  term of its lowering, the result of a function or array sort, the width of
   *  a bit-vector.
   */
  final case class SortEntry(text: String, key: String, lowered: String, result: Option[Int], width: Option[Int])

  /** A real rule (`Vik.Lower.RRule`). */
  enum RRule:
    case Rat(p: BigInt, q: BigInt)
    case RatMul(p: BigInt, q: BigInt, t: Term)
    case Floor(t: Term)

  /** A rule of the lowering of bit-vectors (`Vik.Lower.Rule`), over lowered
   *  terms.
   */
  enum Rule:
    case Modulo(v: Term, w: Int, lo: BigInt, hi: BigInt)
    case Cases(c: Formula, t: Term, e: Term, lo: BigInt, hi: BigInt)
    case Divide(v: Term, d: BigInt, qmax: BigInt)
    case SDivide(t: Term, w: Int, d: BigInt, floor: Boolean)
    case Int2Bv(t: Term, w: Int)
    case SModZ(a: Term, c: BigInt, w: Int)
    case Bits(t: Term, w: Int)
    case UDiv(a: Term, b: Term, w: Int)

  /** Why the lowering minted a symbol (`Vik.Lower.Mint`). */
  enum Mint:
    case Ite(node: Int, sort: Int)
    case Store(sort: Int, index: Int, element: Int)
    case Real(rule: RRule)
    case Rule(rule: LeanExport.Rule, j: Int)

  /** Why a lowered input holds (`Vik.Lower.InputWhy`). */
  enum Reason:
    case Assert(node: Int)
    case IteThen(symbol: String)
    case IteElse(symbol: String)
    case Range(node: Int)
    case Real(rule: RRule, fact: Int)
    case Rule(rule: LeanExport.Rule, fact: Int, aux: Option[Term])

  def apply(in: Input, surface: Option[Surface] = None): Either[String, String] =
    try Right(Writer(in, surface).write())
    catch case e: Unsupported => Left(e.getMessage)

  private def lit(l: Lit): String = s"⟨${l.variable}, ${l.positive}⟩"
  private def litOfIndex(i: Int): String = lit(Lit.fromIndex(i))
  private def int(v: BigInt): String = if v < 0 then s"($v)" else v.toString
  private def rat(r: Rational): String =
    if r.isInteger then int(r.num) else s"((${r.num} : Rat) / ${r.den})"
  private def list(xs: Iterable[String]): String = xs.mkString("[", ", ", "]")

  private final class Writer(in: Input, surface: Option[Surface]):
    import in.*

    // Sorts: `Bool`, `Int` and `Real` first, the others as met. A bit-vector
    // index is an integer sort of its own, in its range.
    private val sorts = ArrayBuffer(".bool", ".int", ".real")
    private val sortIds = HashMap[String, Int]("Bool" -> 0, "Int" -> 1, "Real" -> 2)
    private val bvIndex = ArrayBuffer.empty[(Int, Int)]
    private val bitVector = """\(_ BitVec (\d+)\)""".r
    private val results = HashMap.empty[Int, Int]
    private def newSort(key: String, text: String): Int = sortIds.getOrElseUpdate(
      key, {
        sorts += text
        sorts.length - 1
      }
    )
    private def fnSort(params: List[Int], result: Int): Int =
      val text = s".fn ${list(params.map(_.toString))} $result"
      val i = newSort(text, text)
      results.getOrElseUpdate(i, result)
      i
    private def sortId(s: String): Int = sortIds.get(s) match
      case Some(i) => i
      case None =>
        Sorts.arrow(s) match
          case Some((params, result)) =>
            val i = fnSort(params.map(sortId), sortId(result))
            sortIds(s) = i
            i
          case None =>
            s match
              case "?" => throw Unsupported("a term of unknown sort")
              case bitVector(w) =>
                val i = newSort(s, ".int")
                bvIndex += ((i, w.nn.toInt))
                i
              case _ => newSort(s, ".user")

    // Symbols, by name, with their sorts: a `store!` symbol takes an array, an
    // index and an element to an array.
    private val decls = LinkedHashMap.empty[String, Int]
    private val declSorts = HashMap.empty[String, Int]
    // With a surface script, its sorts come first, then the sorts of the stores;
    // its symbols, then the minted ones.
    for sf <- surface do
      sorts.clear()
      sortIds.clear()
      for (e, i) <- sf.sorts.zipWithIndex do
        sorts += e.lowered
        sortIds.getOrElseUpdate(e.key, i)
        sortIds.getOrElseUpdate(e.lowered, i)
        for r <- e.result do results(i) = r
        for w <- e.width do bvIndex += ((i, w))
      for (name, s) <- sf.decls do
        declSorts(name) = s
        decls(name) = decls.size
      for (name, m) <- sf.mints do
        declSorts(name) = m match
          case Mint.Ite(_, s) => s
          case Mint.Store(s, idx, el) => fnSort(List(s, idx, el), s)
          case Mint.Real(RRule.Floor(_)) | Mint.Rule(_, _) => 1
          case Mint.Real(_) => 2
        decls(name) = decls.size

    private def symbolSort(name: String): String = declarations.get(name) match
      case Some((args, result)) => Sorts.function(args, result)
      case None => introduced.getOrElse(name, throw Unsupported(s"the symbol $name"))
    private def decl(name: String): Int = decls.getOrElseUpdate(
      name, {
        val s = symbolSort(name)
        declSorts(name) =
          if name.startsWith("store!") then
            Sorts.arrow(s) match
              case Some((arr :: idx :: el :: _, _)) =>
                val a = sortId(Sorts.function(List(idx), el))
                fnSort(List(a, sortId(idx), sortId(el)), a)
              case _ => throw Unsupported(s"the store $name")
          else sortId(s)
        decls.size
      }
    )

    // Nodes, shared when equal.
    private val nodes = ArrayBuffer.empty[String]
    private val nodeSorts = ArrayBuffer.empty[Int]
    private val nodeIds = HashMap.empty[String, Int]
    private def node(text: String, sort: Int): Int = nodeIds.getOrElseUpdate(
      text, {
        nodes += text
        nodeSorts += sort
        nodes.length - 1
      }
    )

    private val termIds = HashMap.empty[Term, Int]
    def term(t: Term): Int = termIds.getOrElseUpdate(
      t,
      t match
        case Term.Const(name) =>
          val d = decl(name)
          node(s".const $d", declSorts(name))
        case Term.App(fun, args) =>
          val full = fun match
            case Term.Const(n) if n.startsWith("store!") => 3
            case _ => Sorts.arrow(sortOf(fun)).fold(-1)(_._1.length)
          if args.length != full then throw Unsupported(s"the partial application $t")
          val f = term(fun)
          val as = args.map(term)
          val result = fun match
            case Term.Const(n) => results.get(declSorts(n))
            case _ => None
          node(s".app $f ${list(as.map(_.toString))}", result.getOrElse(sortId(sortOf(t))))
        case Term.Num(v) => node(s".num ${int(v)}", 1)
        case Term.Linear(ts, c) =>
          val parts = ts.map((a, u) => s"(${int(a)}, ${term(u)})")
          node(s".linear ${list(parts)} ${int(c)}", sortId(sortOf(t)))
        case Term.Bool(f) => node(s".bterm ${formula(f)}", 0)
    )

    private val formulaIds = HashMap.empty[Formula, Int]
    def formula(f: Formula): Int = formulaIds.getOrElseUpdate(
      f, {
        import Formula.*
        val text = f match
          case True => ".tt"
          case False => ".ff"
          case Not(a) => s".not ${formula(a)}"
          case And(as) => s".and ${list(as.map(formula(_).toString))}"
          case Or(as) => s".or ${list(as.map(formula(_).toString))}"
          case Implies(a, b) => s".implies ${formula(a)} ${formula(b)}"
          case Iff(a, b) => s".iff ${formula(a)} ${formula(b)}"
          case Ite(c, a, b) => s".ite ${formula(c)} ${formula(a)} ${formula(b)}"
          case Eq(a, b) => s".eq ${term(a)} ${term(b)}"
          case Pred(t) => s".pred ${term(t)}"
          case Le(a, b) => s".le ${term(a)} ${term(b)}"
          case Less(a, b) => s".lt ${term(a)} ${term(b)}"
        node(text, 0)
      }
    )

    // Classes: a leaf is the node of its term, `true` and `false` those of the
    // Boolean terms, an application that of its function applied to its arguments.
    private val apps = HashMap.empty[EClassId, App]
    private val classIds = HashMap.empty[EClassId, Int]
    private val classSorts = HashMap.empty[EClassId, String]
    private def classSort(x: EClassId): String = classSorts.getOrElseUpdate(
      x,
      classTerms.get(x) match
        case Some(t) => sortOf(t)
        case None =>
          apps.get(x) match
            case Some(a) => Sorts.applied(classSort(a.fun), a.args.length)
            case None => throw Unsupported(s"the class $x")
    )
    def classNode(x: EClassId): Int = classIds.getOrElseUpdate(
      x,
      if x == trueClass then term(Term.Bool(Formula.True))
      else if x == falseClass then term(Term.Bool(Formula.False))
      else
        apps.get(x) match
          case Some(a) =>
            val f = classNode(a.fun)
            val as = a.args.map(classNode)
            node(s".app $f ${list(as.map(_.toString))}", sortId(classSort(x)))
          case None =>
            classTerms.get(x) match
              case Some(t) => term(t)
              case None => throw Unsupported(s"the class $x")
    )

    // The proof's steps and their justifications.
    private def ufSteps(proofs: Seq[EqProof]): (List[String], Seq[Int]) =
      val steps = ArrayBuffer.empty[String]
      val seen = java.util.IdentityHashMap[EqProof, Integer]()
      def go(p: EqProof): Int =
        val known = seen.get(p)
        if known != null then known
        else
          val text = p match
            case EqProof.Refl(a) => s".refl ${classNode(a)}"
            case EqProof.Given(a, b, r) => s".given ${classNode(a)} ${classNode(b)} ${litOfIndex(r)}"
            case EqProof.Symm(q) => s".symm ${go(q)}"
            case EqProof.Trans(q, r) =>
              val i = go(q)
              s".trans $i ${go(r)}"
            case EqProof.Cong(a, b, f, args) =>
              val fi = go(f)
              val as = args.map(go)
              s".cong ${classNode(a)} ${classNode(b)} $fi ${list(as.map(_.toString))}"
            case EqProof.Rule(a, b, premises) =>
              val ps = premises.map(go)
              s".rule ${classNode(a)} ${classNode(b)} ${list(ps.map(_.toString))}"
          steps += text
          seen.put(p, steps.length - 1)
          steps.length - 1
      val roots = proofs.map(go)
      (steps.toList, roots)

    private def ufJust(c: UfTheory.Certified): String =
      apps ++= c.nodes
      c.certificate match
        case UfTheory.Certificate.Conflict(EConflict.Disequal(p, reason)) =>
          val (steps, Seq(i)) = ufSteps(Seq(p)): @unchecked
          s".uf ${list(steps)} (.disequal $i ${litOfIndex(reason)})"
        case UfTheory.Certificate.Conflict(EConflict.Contradiction(ps)) =>
          val (steps, is) = ufSteps(ps)
          s".uf ${list(steps)} (.contradiction ${list(is.map(_.toString))})"
        case UfTheory.Certificate.Equal(p) =>
          val (steps, Seq(i)) = ufSteps(Seq(p)): @unchecked
          s".uf ${list(steps)} (.equal $i)"
        case UfTheory.Certificate.Apart(p, q, reason) =>
          val (steps, Seq(i, j)) = ufSteps(Seq(p, q)): @unchecked
          s".uf ${list(steps)} (.apart $i $j ${litOfIndex(reason)})"

    private def liaJust(c: lia.Certificate): String =
      import lia.Certificate.*
      val out = ArrayBuffer.empty[String]
      val seen = java.util.IdentityHashMap[lia.Certificate, Integer]()
      def hyp(h: Hyp): String = s"⟨${int(h.reason)}, ${h.x}, ${int(h.c)}, ${h.strict}, ${h.upper}⟩"
      def go(c: lia.Certificate): Int =
        val known = seen.get(c)
        if known != null then known
        else
          val text = c match
            case h: Hyp => s".hyp ${hyp(h)}"
            case Comb(factors, parts) =>
              val ps = parts.map(go)
              s".comb ${list(factors.indices.map(i => s"(${rat(factors(i))}, ${ps(i)})"))}"
            case Round(of) => s".round ${go(of)}"
            case Split(reason, x, floor, ceil, low, high, except, at, strict) =>
              val l = go(low)
              val h = go(high)
              s".split ${int(reason)} $x ${int(floor)} ${int(ceil)} $l $h ${int(except)} ${int(at)} $strict"
          out += text
          seen.put(c, out.length - 1)
          out.length - 1
      go(c)
      s".lia ${list(out)}"

    private def meaningOf(l: Lit): Formula | Null =
      if l.variable < meanings.length then meanings(l.variable) else null

    // The extensionality instances by their witnesses, with their equalities,
    // and the witnesses of ranges.
    private val exts = LinkedHashMap.empty[List[Term], (Int, Int)]
    private val ranges = scala.collection.mutable.LinkedHashSet.empty[Term]

    private def origin(clause: Array[Lit], o: AnyRef | Null): String = o match
      case Encoder.Origin.Definition => ".definition"
      case Encoder.Origin.Assertion(f) => s".assertion ${formula(f)}"
      case Encoder.Origin.Retired => ".retired"
      case Encoder.Origin.Numerals => ".numerals"
      case Encoder.Origin.SharedSplit =>
        // the formulas `a < b` and `a ≤ b` of the clause
        val lt = clause.iterator.map(meaningOf).collectFirst { case f @ Formula.Less(_, _) => f }
        val le = clause.iterator.map(meaningOf).collectFirst { case f @ Formula.Le(_, _) => f }
        (lt, le) match
          case (Some(lt), Some(le)) => s".sharedSplit ${formula(lt)} ${formula(le)}"
          case (Some(lt @ Formula.Less(a, b)), None) => s".sharedSplit ${formula(lt)} ${formula(Formula.Le(a, b))}"
          case _ => throw Unsupported("a shared split without its comparisons")
      case Encoder.Origin.ReadOverWrite => ".readOverWrite"
      case Encoder.Origin.Congruence =>
        // `f ≠ g ∨ f(x) = g(x)`: the equality `f = g`
        clause.iterator.filter(_.positive).map(meaningOf).collectFirst {
          case Formula.Eq(Term.App(f, xs), Term.App(g, ys)) if xs == ys => formula(Formula.Eq(f, g))
        } match
          case Some(eq) => s".congruence $eq"
          case None => throw Unsupported("a congruence without its equality")
      case Encoder.Origin.Exhaustive(t) =>
        val d = datatypes.find(_.name == sortOf(t)).getOrElse(throw Unsupported(s"the datatype of $t"))
        val or = Formula.Or(d.constructors.map(c => Formula.Pred(Term.App(Term.Const(c.tester), List(t)))))
        s".exhaustive ${term(t)} ${formula(or)}"
      case Encoder.Origin.Extensionality(ws) =>
        def at(f: Term): Term = ws.foldLeft(f)((t, w) => Term.App(t, List(w)))
        val negatives = clause.iterator.filter(!_.positive).map(meaningOf).toSet
        clause.iterator.filter(_.positive).map(meaningOf).collectFirst {
          case eq @ Formula.Eq(f, g) if negatives.contains(Formula.Eq(at(f), at(g))) => (eq, Formula.Eq(at(f), at(g)))
        } match
          case Some((eq, eqw)) =>
            val k = exts.getOrElseUpdate(ws, (formula(eq), formula(eqw)))
            s".extensionality ${exts.keysIterator.indexOf(ws)}"
          case None => throw Unsupported("an extensionality instance without its equalities")
      case Encoder.Origin.WitnessRange(w, width) =>
        ranges += w
        val lo = Formula.Le(Term.Num(0), w)
        val hi = Formula.Le(w, Term.Num(BigInt(2).pow(width) - 1))
        val f = if clause.exists(l => l.positive && meaningOf(l) == lo) then lo else hi
        s".witnessRange ${formula(f)}"
      case other => throw Unsupported(s"the origin $other")

    private def rrule(r: RRule): String = r match
      case RRule.Rat(p, q) => s".rat ${int(p)} $q"
      case RRule.RatMul(p, q, t) => s".ratmul ${int(p)} $q ${term(t)}"
      case RRule.Floor(t) => s".floor ${term(t)}"

    private def rule(r: Rule): String = r match
      case Rule.Modulo(v, w, lo, hi) => s".modulo ${term(v)} $w ${int(lo)} ${int(hi)}"
      case Rule.Cases(c, t, e, lo, hi) => s".cases ${formula(c)} ${term(t)} ${term(e)} ${int(lo)} ${int(hi)}"
      case Rule.Divide(v, d, qmax) => s".divide ${term(v)} ${int(d)} ${int(qmax)}"
      case Rule.SDivide(t, w, d, floor) => s".sdivide ${term(t)} $w ${int(d)} $floor"
      case Rule.Int2Bv(t, w) => s".int2bv ${term(t)} $w"
      case Rule.SModZ(a, c, w) => s".smodz ${term(a)} ${int(c)} $w"
      case Rule.Bits(t, w) => s".bits ${term(t)} $w"
      case Rule.UDiv(a, b, w) => s".udiv ${term(a)} ${term(b)} $w"

    private def reason(sf: Surface, f: Formula): String = sf.reasons.get(f) match
      case Some(Reason.Assert(a)) => s".assert $a"
      case Some(Reason.IteThen(k)) => s".iteThen ${decls(k)}"
      case Some(Reason.IteElse(k)) => s".iteElse ${decls(k)}"
      case Some(Reason.Range(i)) => s".range $i"
      case Some(Reason.Real(r, j)) => s".real (${rrule(r)}) $j"
      case Some(Reason.Rule(r, j, aux)) => s".rule (${rule(r)}) $j ${aux.fold(0)(term)}"
      case None => throw Unsupported(s"the input $f, of no reason")

    /** The script and the trace, written before the lowered problem: they add
     *  the nodes of the lowerings of the surface nodes.
     */
    private def surfaceText(sf: Surface): String =
      val node = sf.values.map {
        case t: Term => term(t)
        case f: Formula => formula(f)
      }
      val mints = sf.mints.map((_, m) =>
        m match
          case Mint.Ite(i, _) => s".ite $i"
          case Mint.Store(s, _, _) => s".store $s"
          case Mint.Real(r) => s".real (${rrule(r)})"
          case Mint.Rule(r, j) => s".rule (${rule(r)}) $j"
      ) ++ decls.keysIterator.drop(sf.decls.length + sf.mints.length).map { n =>
        // the witnesses of the answer, of the sorts of the script
        val s = declSorts(n)
        if s >= sf.sorts.length then throw Unsupported(s"the symbol $n")
        s".witness $s"
      }
      val reasons = inputs.map(reason(sf, _))
      val levelReasons = assumed.map(v => list(levels.find(_._1 == v).fold(Nil)(_._2).map(reason(sf, _))))
      val datatypes =
        sf.datatypes.map((d, cs) => s"($d, ${list(cs.map((c, t, ss) => s"($c, $t, ${list(ss.map(_.toString))})"))})")
      val b = StringBuilder()
      b ++= "def script : Vik.Lower.SProblem where\n"
      b ++= s"  sig := ⟨${list(sf.sorts.map(_.text))}, ${list(sf.decls.map(_._2.toString))}⟩\n"
      b ++= s"  nodes := ${list(sf.nodes)}\n"
      b ++= s"  sortOf := ${list(sf.sortOf.map(_.toString))}\n"
      b ++= s"  asserts := ${list(sf.asserts.map(_.toString))}\n"
      b ++= s"  datatypes := ${list(datatypes)}\n"
      b ++= s"  defs := ${list(sf.defs)}\n\n"
      b ++= "def trace : Vik.Lower.Trace where\n"
      b ++= s"  node := ${list(node.map(_.toString))}\n"
      b ++= s"  mints := ${list(mints)}\n"
      b ++= s"  inputs := ${list(reasons)}\n"
      b ++= s"  levels := ${list(levelReasons)}\n"
      b ++= s"  insts := ${list(sf.insts.map((i, ms) => s"($i, ${list(ms.map(_.toString))})"))}\n\n"
      b.toString

    private val surfaceTheorems =
      """|theorem checked : trace.check script problem answer = true := by decide +kernel
         |
         |/-- No model of the script satisfies its assertions. -/
         |theorem no_model {R U : Type} [Lean.Grind.Field R] [LE R] [LT R] [Std.IsLinearOrder R]
         |    [Std.LawfulOrderLT R] [Lean.Grind.OrderedRing R] [Lean.Grind.IsCharP R 0] (M : Vik.Surf.Model R U)
         |    (size : Val R U → Nat) (hL : script.Laws M size) (hfl : ∀ r : R, ∃ k : Int, Vik.Real.IsFloor r k)
         |    (h : ∀ a ∈ script.asserts, Vik.Surf.holds M.toInterp script.nodes a) : False :=
         |  Vik.Lower.Trace.unsat checked accepted M size hL hfl h
         |""".stripMargin

    def write(): String =
      val steps = ArrayBuffer.empty[String]
      val justs = ArrayBuffer.empty[String]
      // the steps the conclusion rests on, renumbered, and their variables
      val needed = Array.fill(proof.conclusion + 1)(false)
      needed(proof.conclusion) = true
      for i <- proof.conclusion to 0 by -1 if needed(i) do
        proof.steps(i) match
          case cdclt.Proof.Step.Derived(_, hints) => hints.foreach(needed(_) = true)
          case _ => ()
      val kept = (0 to proof.conclusion).filter(needed)
      val index = kept.zipWithIndex.toMap
      val used = scala.collection.mutable.HashSet(trueLit.variable)
      used ++= levels.map(_._1)
      val last = kept.length - 1
      for i <- kept do
        val step = proof.steps(i)
        step match
          case cdclt.Proof.Step.Input(clause, _) => used ++= clause.map(_.variable)
          case cdclt.Proof.Step.Lemma(clause, _) => used ++= clause.map(_.variable)
          case cdclt.Proof.Step.Derived(clause, _) => used ++= clause.map(_.variable)
        step match
          case cdclt.Proof.Step.Input(clause, o) =>
            steps += s".input ${list(clause.map(lit))}"
            justs += s".input (${origin(clause, o)})"
          case cdclt.Proof.Step.Lemma(clause, c) =>
            steps += s".lemma ${list(clause.map(lit))}"
            justs +=
              (c match
                case c: UfTheory.Certified => ufJust(c)
                case c: lia.Certificate => liaJust(c)
                case _ => throw Unsupported("a lemma without a certificate"))
          case cdclt.Proof.Step.Derived(clause, hints) =>
            steps += s".derived ${list(clause.map(lit))} ${list(hints.map(index(_).toString))}"
            justs += ".derived"
      // the formulas and atoms of the variables
      val vars = math.max(meanings.length, atoms.length)
      val meaning = (0 until vars).map { v =>
        val m = if v < meanings.length && used(v) then meanings(v) else null
        if m == null then "none" else s"some ${formula(m)}"
      }
      val ufAtoms = (0 until vars).map { v =>
        val a = if v < atoms.length && used(v) then atoms(v) else null
        if a == null then "none" else s"some (${classNode(a._1)}, ${classNode(a._2)})"
      }
      val liaAtomsText = (0 until vars).map { v =>
        (if v < liaAtoms.length && used(v) then liaAtoms(v) else null) match
          case LiaTheory.Atom.Bound(x, c, strict) => s"some (.bound $x ${int(c)} $strict)"
          case LiaTheory.Atom.Equal(x, c) => s"some (.equal $x ${int(c)})"
          case null => "none"
      }
      // each form over the nodes of its unknowns, sorted, as `Lia.add` merges them
      val liaForms = (0 until liaVars).map { x =>
        val f = liaForm(x).toList
          .map((u, a) => (term(unknowns.getOrElse(u, throw Unsupported(s"the unknown $u"))), a))
          .groupMapReduce(_._1)(_._2)(_ + _)
          .toList
          .filter(_._2 != lia.Rational.zero)
          .sortBy(_._1)
        list(f.map((n, a) => s"($n, ${rat(a)})"))
      }
      val inputIds = inputs.map(formula)
      val levelsText = levels.map((v, fs) => s"($v, ${list(fs.map(formula(_).toString))})")
      // datatypes, with the classes of their constructors and testers
      val dts = datatypes.map { d =>
        val cs = d.constructors.map(c =>
          s"(${decl(c.name)}, ${decl(c.tester)}, ${list(c.selectors.map((n, _) => decl(n).toString))})"
        )
        s"(${sortId(d.name)}, ${list(cs)})"
      }
      val ctors = datatypes.flatMap(_.constructors).map(c =>
        s"(${term(Term.Const(c.name))}, ${list(c.selectors.map((n, _) => term(Term.Const(n)).toString))})"
      )
      val testers = datatypes.flatMap(_.constructors).map(c =>
        s"(${term(Term.Const(c.tester))}, ${term(Term.Const(c.name))})"
      )
      // the witnesses, in the order the encoder made them
      def witnessOrder(w: Term): Int = w match
        case Term.Const(n) => n.stripPrefix("ext!").toIntOption.getOrElse(Int.MaxValue)
        case _ => Int.MaxValue
      def sym(w: Term): Int = w match
        case Term.Const(n) => decl(n)
        case _ => throw Unsupported(s"the witness $w")
      val extList = exts.toList
      val order = extList.indices.sortBy(k => extList(k)._1.map(witnessOrder).minOption.getOrElse(Int.MaxValue))
      if order != extList.indices then throw Unsupported("extensionality instances out of the order of their witnesses")
      val extsText = extList.map((ws, eqs) => s"(${eqs._1}, ${eqs._2}, ${list(ws.map(sym(_).toString))})")
      val witnessSyms = (extList.flatMap(_._1) ++ ranges.filterNot(w => extList.exists(_._1.contains(w)))).map(sym)
      val surfaceDefs = surface.map(surfaceText)
      val trueNode = classNode(trueClass)
      val falseNode = classNode(falseClass)
      formula(Formula.True)
      val declList = decls.keysIterator.map(declSorts(_).toString).toList
      val stores = decls.keysIterator.filter(_.startsWith("store!")).map(decls(_).toString).toList
      val b = StringBuilder()
      b ++= "def problem : Problem where\n"
      b ++= s"  sig := ⟨${list(sorts)}, ${list(declList)}⟩\n"
      b ++= s"  nodes := ${list(nodes)}\n"
      b ++= s"  sortOf := ${list(nodeSorts.map(_.toString))}\n"
      b ++= s"  inputs := ${list(inputIds.map(_.toString))}\n"
      val dtsText = surface.fold(list(dts))(sf =>
        list(sf.datatypes.map((d, cs) =>
          s"($d, ${list(cs.map((c, t, ss) => s"($c, $t, ${list(ss.map(_.toString))})"))})"
        ))
      )
      b ++= s"  datatypes := $dtsText\n"
      b ++= s"  stores := ${list(stores)}\n"
      b ++= s"  bvIndex := ${list(bvIndex.map((s, w) => s"($s, $w)"))}\n\n"
      b ++= "def answer : Answer where\n"
      b ++= s"  meaning := ${list(meaning)}\n"
      b ++= s"  trueLit := ${lit(trueLit)}\n"
      b ++= s"  levels := ${list(levelsText)}\n"
      b ++= s"  assumed := ${list(assumed.map(_.toString))}\n"
      b ++= s"  ufAtoms := ${list(ufAtoms)}\n"
      b ++= s"  trueClass := $trueNode\n  falseClass := $falseNode\n"
      b ++= s"  ufCtors := ${list(ctors)}\n"
      b ++= s"  ufTesters := ${list(testers)}\n"
      b ++= s"  liaAtoms := ${list(liaAtomsText)}\n"
      b ++= s"  liaForms := ${list(liaForms)}\n"
      b ++= s"  steps := ${list(steps)}\n"
      b ++= s"  justs := ${list(justs)}\n"
      b ++= s"  conclusion := $last\n"
      b ++= s"  exts := ${list(extsText)}\n"
      b ++= s"  witnesses := ${list(witnessSyms.map(_.toString))}\n\n"
      b ++= "theorem accepted : problem.accepts answer = true := by decide +kernel\n"
      for text <- surfaceDefs do
        b ++= "\n" + text + surfaceTheorems
      b.toString
