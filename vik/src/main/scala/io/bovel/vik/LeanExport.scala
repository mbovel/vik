package io.bovel.vik

import scala.collection.mutable.{ArrayBuffer, HashMap, LinkedHashMap}

import io.bovel.cdclt
import io.bovel.cdclt.Lit
import io.bovel.egraph.{App, EClassId, EConflict, EqProof}
import io.bovel.lia
import io.bovel.smtlib.Sorts

/** Writes the proof of an `Unsat` as Lean terms that `proofs/lean` checks: the
 *  lowered problem (`Vik.Low.Problem`), the answer (`Vik.Low.Answer`) and the
 *  theorem that `Problem.accepts` accepts it, by `decide +kernel`.
 *
 *  Terms and formulas become one table of nodes, each after its children,
 *  shared when equal. An e-class is the node of its term: a leaf the node of
 *  the term the encoder made it for, an application that of its function
 *  applied to its arguments. An LIA unknown is the node of its term, and a
 *  variable the expression it stands for over them.
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
      inputs: List[Formula],
      levels: List[(cdclt.Var, List[Formula])],
      assumed: List[cdclt.Var]
  )

  private type Rational = lia.Rational

  /** Not supported yet by the export. */
  final class Unsupported(msg: String) extends Exception(msg)

  def apply(in: Input): Either[String, String] =
    try Right(Writer(in).write())
    catch case e: Unsupported => Left(e.getMessage)

  private def lit(l: Lit): String = s"⟨${l.variable}, ${l.positive}⟩"
  private def litOfIndex(i: Int): String = lit(Lit.fromIndex(i))
  private def int(v: BigInt): String = if v < 0 then s"($v)" else v.toString
  private def rat(r: Rational): String =
    if r.isInteger then int(r.num) else s"((${r.num} : Rat) / ${r.den})"
  private def list(xs: Iterable[String]): String = xs.mkString("[", ", ", "]")

  private final class Writer(in: Input):
    import in.*

    // Sorts: `Bool`, `Int` and `Real` first, the others as met.
    private val sorts = LinkedHashMap[String, String]("Bool" -> ".bool", "Int" -> ".int", "Real" -> ".real")
    private val sortIds = HashMap[String, Int]("Bool" -> 0, "Int" -> 1, "Real" -> 2)
    private def sortId(s: String): Int = sortIds.get(s) match
      case Some(i) => i
      case None =>
        val d = Sorts.arrow(s) match
          case Some((params, result)) =>
            val ps = params.map(sortId)
            s".fn ${list(ps.map(_.toString))} ${sortId(result)}"
          case None =>
            if s == "?" then throw Unsupported("a term of unknown sort")
            ".user"
        val i = sorts.size
        sorts(s) = d
        sortIds(s) = i
        i

    // Symbols, by name, with their sorts.
    private val decls = LinkedHashMap.empty[String, Int]
    private def decl(name: String): Int = decls.getOrElseUpdate(
      name, {
        val sort = declarations.get(name) match
          case Some((args, result)) => Sorts.function(args, result)
          case None => introduced.getOrElse(name, throw Unsupported(s"the symbol $name"))
        sortId(sort)
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
          node(s".const $d", sortId(sortOf(t)))
        case Term.App(fun, args) =>
          val f = term(fun)
          val as = args.map(term)
          node(s".app $f ${list(as.map(_.toString))}", sortId(sortOf(t)))
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

    private def origin(clause: Array[Lit], o: AnyRef | Null): String = o match
      case Encoder.Origin.Definition => ".definition"
      case Encoder.Origin.Assertion(f) => s".assertion ${formula(f)}"
      case Encoder.Origin.Retired => ".retired"
      case Encoder.Origin.Numerals => ".numerals"
      case Encoder.Origin.SharedSplit =>
        // the formulas `a < b` and `a ≤ b` of the clause
        def meaningOf(l: Lit): Formula | Null =
          if l.variable < meanings.length then meanings(l.variable) else null
        val lt = clause.iterator.map(meaningOf).collectFirst { case f @ Formula.Less(_, _) => f }
        val le = clause.iterator.map(meaningOf).collectFirst { case f @ Formula.Le(_, _) => f }
        (lt, le) match
          case (Some(lt), Some(le)) => s".sharedSplit ${formula(lt)} ${formula(le)}"
          case (Some(lt @ Formula.Less(a, b)), None) => s".sharedSplit ${formula(lt)} ${formula(Formula.Le(a, b))}"
          case _ => throw Unsupported("a shared split without its comparisons")
      case other => throw Unsupported(s"the origin $other")

    def write(): String =
      val steps = ArrayBuffer.empty[String]
      val justs = ArrayBuffer.empty[String]
      val last = proof.conclusion
      for i <- 0 to last do
        proof.steps(i) match
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
            steps += s".derived ${list(clause.map(lit))} ${list(hints.map(_.toString))}"
            justs += ".derived"
      // the formulas and atoms of the variables
      val vars = math.max(meanings.length, atoms.length)
      val meaning = (0 until vars).map { v =>
        val m = if v < meanings.length then meanings(v) else null
        if m == null then "none" else s"some ${formula(m)}"
      }
      val ufAtoms = (0 until vars).map { v =>
        val a = if v < atoms.length then atoms(v) else null
        if a == null then "none" else s"some (${classNode(a._1)}, ${classNode(a._2)})"
      }
      val liaAtomsText = (0 until vars).map { v =>
        (if v < liaAtoms.length then liaAtoms(v) else null) match
          case LiaTheory.Atom.Bound(x, c, strict) => s"some (.bound $x ${int(c)} $strict)"
          case LiaTheory.Atom.Equal(x, c) => s"some (.equal $x ${int(c)})"
          case null => "none"
      }
      val liaForms = (0 until liaVars).map { x =>
        val f = liaForm(x).toList.map((u, a) =>
          s"(${term(unknowns.getOrElse(u, throw Unsupported(s"the unknown $u")))}, ${rat(a)})"
        )
        list(f)
      }
      val inputIds = inputs.map(formula)
      val levelsText = levels.map((v, fs) => s"($v, ${list(fs.map(formula(_).toString))})")
      val trueNode = classNode(trueClass)
      val falseNode = classNode(falseClass)
      formula(Formula.True)
      val sortList = sorts.values.toList
      val declList = decls.toList.map((name, _) =>
        val sort = declarations.get(name) match
          case Some((args, result)) => Sorts.function(args, result)
          case None => introduced(name)
        sortId(sort).toString
      )
      val b = StringBuilder()
      b ++= "def problem : Problem where\n"
      b ++= s"  sig := ⟨${list(sorts.values)}, ${list(declList)}⟩\n"
      b ++= s"  nodes := ${list(nodes)}\n"
      b ++= s"  sortOf := ${list(nodeSorts.map(_.toString))}\n"
      b ++= s"  inputs := ${list(inputIds.map(_.toString))}\n"
      b ++= "  datatypes := []\n  stores := []\n  bvIndex := []\n\n"
      b ++= "def answer : Answer where\n"
      b ++= s"  meaning := ${list(meaning)}\n"
      b ++= s"  trueLit := ${lit(trueLit)}\n"
      b ++= s"  levels := ${list(levelsText)}\n"
      b ++= s"  assumed := ${list(assumed.map(_.toString))}\n"
      b ++= s"  ufAtoms := ${list(ufAtoms)}\n"
      b ++= s"  trueClass := $trueNode\n  falseClass := $falseNode\n"
      b ++= "  ufCtors := []\n  ufTesters := []\n"
      b ++= s"  liaAtoms := ${list(liaAtomsText)}\n"
      b ++= s"  liaForms := ${list(liaForms)}\n"
      b ++= s"  steps := ${list(steps)}\n"
      b ++= s"  justs := ${list(justs)}\n"
      b ++= s"  conclusion := $last\n"
      b ++= "  exts := []\n  witnesses := []\n\n"
      b ++= "theorem accepted : problem.accepts answer = true := by decide +kernel\n"
      b.toString
