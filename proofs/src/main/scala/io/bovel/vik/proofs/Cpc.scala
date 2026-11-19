package io.bovel.vik.proofs

import scala.collection.mutable.{ArrayBuffer, HashMap, HashSet}

import Sexp.{Atom, Lst}

/** Writes a certificate of Vik (`Session.proof`, see `ProofExport`) as a proof
 *  in CPC, the Cooperating Proof Calculus of cvc5, which Ethos checks against
 *  cvc5's signature `Cpc.eo` together with `proofs/cpc/Vik.eo`.
 *
 *  The proof starts with the declarations of the script's sorts and symbols
 *  and one `assume` per assertion of the script, as written. The rest follows
 *  the answer, one CPC derivation per step:
 *
 *  - an input clause from its origin: the assertion itself, or the CNF rule
 *    of the formula a Tseitin variable stands for;
 *  - a lemma of the theory of uninterpreted functions as a scope over the
 *    negations of its literals, inside which the equality proof derives
 *    `false`;
 *  - a derived clause as one `chain_m_resolution` over its hints, the order
 *    and pivots found by replaying the unit propagation.
 *
 *  A Tseitin variable is the formula it stands for, so the proof has no
 *  variables of its own. Terms are shared with `define`, one per node of the
 *  script or of the lowered problem that has children.
 *
 *  This covers Booleans, uninterpreted sorts and functions. Anything else
 *  throws [[Unsupported]].
 */
object Cpc:

  /** Not a certificate this driver reads. */
  final class Malformed(message: String) extends Exception(message)

  /** A certificate this driver does not translate yet. */
  final class Unsupported(message: String) extends Exception(message)

  /** A literal of the answer: a variable, positive or negated. */
  private final case class Lit(v: Int, pos: Boolean):
    def neg: Lit = Lit(v, !pos)

  def apply(certificate: String): String = Writer(certificate).write()

  private def num(s: Sexp): BigInt = s match
    case Atom(t) => BigInt(t)
    case _ => throw Malformed(s"not a number: ${s.toString.take(100)}")
  private def int(s: Sexp): Int = num(s).toInt
  private def items(s: Sexp): Vector[Sexp] = s match
    case Lst(xs) => xs
    case _ => throw Malformed(s"not a list: ${s.toString.take(100)}")
  private def ints(s: Sexp): Vector[Int] = items(s).map(int)
  private def bool(s: Sexp): Boolean = s match
    case Atom("true") => true
    case Atom("false") => false
    case _ => throw Malformed(s"not a Boolean: ${s.toString.take(100)}")
  private def lit(s: Sexp): Lit = s match
    case Lst(Vector(v, p)) => Lit(int(v), bool(p))
    case _ => throw Malformed(s"not a literal: ${s.toString.take(100)}")
  private def option(s: Sexp): Option[Sexp] = s match
    case Atom("none") => None
    case Lst(Vector(Atom("some"), x)) => Some(x)
    case _ => throw Malformed(s"not an option: ${s.toString.take(100)}")

  /** The head and arguments of a constructor application. */
  private object Ctor:
    def unapply(s: Sexp): Option[(String, Vector[Sexp])] = s match
      case Atom(name) => Some((name, Vector.empty))
      case Lst(Atom(name) +: args) => Some((name, args))
      case _ => None

  private final class Writer(certificate: String):
    private val sections: Map[String, Map[String, Sexp]] = Sexp.parse(certificate) match
      case Lst(Atom("vik-proof") +: Atom("1") +: rest) =>
        rest.map {
          case Lst(Atom(name) +: fields) =>
            name -> fields.map {
              case Lst(Vector(Atom(f), v)) => f -> v
              case s => throw Malformed(s"not a field: ${s.toString.take(100)}")
            }.toMap
          case s => throw Malformed(s"not a section: ${s.toString.take(100)}")
        }.toMap
      case Lst(Atom("vik-proof") +: v +: _) => throw Malformed(s"unknown version $v")
      case _ => throw Malformed("not a Vik certificate")

    private def field(section: String, f: String): Sexp =
      sections.getOrElse(section, throw Malformed(s"no $section")).getOrElse(f, throw Malformed(s"no $f in $section"))

    // The script.
    private val (sSorts, sDecls) = field("script", "sig") match
      case Lst(Vector(sorts, decls)) => (items(sorts), ints(decls))
      case s => throw Malformed(s"not a signature: ${s.toString.take(100)}")
    private val sNodes = items(field("script", "nodes"))
    private val asserts = ints(field("script", "asserts"))
    if items(field("script", "datatypes")).nonEmpty then throw Unsupported("datatypes")
    if items(field("script", "defs")).nonEmpty then throw Unsupported("define-fun")

    // The trace.
    private val mints = items(field("trace", "mints"))
    private val inputWhys = items(field("trace", "inputs"))
    if mints.nonEmpty then throw Unsupported("minted symbols")
    if items(field("trace", "levels")).nonEmpty then throw Unsupported("levels")

    // The lowered problem.
    private val lNodes = items(field("problem", "nodes"))
    private val inputs = ints(field("problem", "inputs"))
    if items(field("problem", "datatypes")).nonEmpty then throw Unsupported("datatypes")
    if items(field("problem", "stores")).nonEmpty then throw Unsupported("arrays")

    // The answer.
    private val meaning = items(field("answer", "meaning")).map(option(_).map(int))
    private val trueLit = lit(field("answer", "trueLit"))
    private val ufAtoms = items(field("answer", "ufAtoms")).map(option(_).map(p => ints(p) match
      case Vector(a, b) => (a, b)
      case _ => throw Malformed(s"not a pair: $p")))
    private val trueClass = int(field("answer", "trueClass"))
    private val falseClass = int(field("answer", "falseClass"))
    private val steps = items(field("answer", "steps"))
    private val justs = items(field("answer", "justs"))
    private val conclusion = int(field("answer", "conclusion"))
    if items(field("answer", "levels")).nonEmpty || items(field("answer", "assumed")).nonEmpty then
      throw Unsupported("levels")
    if items(field("answer", "exts")).nonEmpty then throw Unsupported("extensionality")
    if items(field("answer", "liaAtoms")).exists(_ != Atom("none")) then throw Unsupported("arithmetic")

    private val out = StringBuilder()
    private var fresh = 0
    private def id(prefix: String): String =
      fresh += 1
      s"@$prefix$fresh"

    // ----------------------------------------------------------------------
    // Sorts and symbols

    private val userSorts = ArrayBuffer.empty[Int]

    private def sort(s: Int): String = sSorts.lift(s) match
      case Some(Atom("bool")) => "Bool"
      case Some(Atom("int")) => throw Unsupported("arithmetic")
      case Some(Atom("real")) => throw Unsupported("arithmetic")
      case Some(Atom("user")) => s"S$s"
      case Some(Ctor("bv", _)) => throw Unsupported("bit-vectors")
      case Some(Ctor("array", _)) => throw Unsupported("arrays")
      case Some(Ctor("fn", Vector(args, res))) => (ints(args).map(sort) :+ sort(int(res))).mkString("(-> ", " ", ")")
      case other => throw Malformed(s"no sort $s: $other")

    private def symbol(k: Int): String = s"x$k"

    private def declarations(): Unit =
      for (s, i) <- sSorts.zipWithIndex if s == Atom("user") do out ++= s"(declare-sort S$i 0)\n"
      for (s, k) <- sDecls.zipWithIndex do out ++= s"(declare-const ${symbol(k)} ${sort(s)})\n"

    // ----------------------------------------------------------------------
    // Terms: each node with children is a `define` of its own.

    private val sDefined = HashMap.empty[Int, String]
    private val lDefined = HashMap.empty[Int, String]

    /** The term of surface node `i`. */
    private def sTerm(i: Int): String =
      sDefined.getOrElseUpdate(
        i, {
          def all(name: String, as: Sexp) = ints(as).map(sTerm).mkString(s"($name ", " ", ")")
          sNodes.lift(i).getOrElse(throw Malformed(s"no surface node $i")) match
            case Ctor("const", Vector(n)) => symbol(int(n))
            case Ctor("tt", _) => "true"
            case Ctor("ff", _) => "false"
            case Ctor("bound", Vector(d)) => sTerm(int(d))
            case Ctor("letIn", Vector(_, body)) => sTerm(int(body))
            case node =>
              val text = node match
                case Ctor("app", Vector(f, args)) => (sTerm(int(f)) +: ints(args).map(sTerm)).mkString("(", " ", ")")
                case Ctor("not", Vector(a)) => s"(not ${sTerm(int(a))})"
                case Ctor("and", Vector(as)) => nary("and", "true", ints(as).map(sTerm))
                case Ctor("or", Vector(as)) => nary("or", "false", ints(as).map(sTerm))
                case Ctor("implies", Vector(as)) => all("=>", as)
                case Ctor("iff", Vector(as)) => all("=", as)
                case Ctor("xor", Vector(as)) => all("xor", as)
                case Ctor("eq", Vector(as)) => all("=", as)
                case Ctor("distinct", Vector(as)) => all("distinct", as)
                case Ctor("ite", Vector(c, a, b)) => s"(ite ${sTerm(int(c))} ${sTerm(int(a))} ${sTerm(int(b))})"
                case Ctor(name, _) => throw Unsupported(s"surface $name")
              define(s"s$i", text)
        }
      )

    /** The term of lowered node `i`. */
    private def lTerm(i: Int): String =
      lDefined.getOrElseUpdate(
        i,
        lNodes.lift(i).getOrElse(throw Malformed(s"no lowered node $i")) match
          case Ctor("const", Vector(n)) =>
            val k = int(n)
            if k >= sDecls.length then throw Unsupported("minted symbols")
            symbol(k)
          case Ctor("tt", _) => "true"
          case Ctor("ff", _) => "false"
          case Ctor("bterm", Vector(f)) => lTerm(int(f))
          case Ctor("pred", Vector(t)) => lTerm(int(t))
          case node =>
            val text = node match
              case Ctor("app", Vector(f, args)) => (lTerm(int(f)) +: ints(args).map(lTerm)).mkString("(", " ", ")")
              case Ctor("not", Vector(a)) => s"(not ${lTerm(int(a))})"
              case Ctor("and", Vector(as)) => nary("and", "true", ints(as).map(lTerm))
              case Ctor("or", Vector(as)) => nary("or", "false", ints(as).map(lTerm))
              case Ctor("implies", Vector(a, b)) => s"(=> ${lTerm(int(a))} ${lTerm(int(b))})"
              case Ctor("iff", Vector(a, b)) => s"(= ${lTerm(int(a))} ${lTerm(int(b))})"
              case Ctor("eq", Vector(a, b)) => s"(= ${lTerm(int(a))} ${lTerm(int(b))})"
              case Ctor("ite", Vector(c, a, b)) => s"(ite ${lTerm(int(c))} ${lTerm(int(a))} ${lTerm(int(b))})"
              case Ctor(name, _) => throw Unsupported(s"lowered $name")
            define(s"t$i", text)
      )

    /** An n-ary connective: its unit with no argument. */
    private def nary(op: String, unit: String, args: Seq[String]): String =
      if args.isEmpty then unit else args.mkString(s"($op ", " ", ")")

    private def define(name: String, text: String): String =
      out ++= s"(define $name () $text)\n"
      name

    // ----------------------------------------------------------------------
    // Literals and clauses

    /** The lowered node a variable stands for. */
    private def meaningOf(v: Int): Int =
      meaning.lift(v).flatten.getOrElse(throw Unsupported(s"variable $v stands for no formula"))

    private def formula(v: Int): String = lTerm(meaningOf(v))

    private def litTerm(l: Lit): String = if l.pos then formula(l.v) else s"(not ${formula(l.v)})"

    private def clauseTerm(c: Seq[Lit]): String = c match
      case Seq() => "false"
      case Seq(l) => litTerm(l)
      case _ => c.map(litTerm).mkString("(or ", " ", ")")

    private def step(conclusion: String, rule: String, premises: Seq[String] = Nil, args: Seq[String] = Nil): String =
      val p = id("p")
      out ++= s"(step $p $conclusion :rule $rule"
      if premises.nonEmpty then out ++= premises.mkString(" :premises (", " ", ")")
      if args.nonEmpty then out ++= args.mkString(" :args (", " ", ")")
      out ++= ")\n"
      p

    // ----------------------------------------------------------------------
    // Steps

    /** The CPC step of each answer step, and the clause it proves, literal by
     *  literal, as the answer gives it.
     */
    private val proved = ArrayBuffer.empty[(String, Vector[Lit])]

    /** The `assume` of each surface assertion. */
    private val assumed = HashMap.empty[Int, String]

    private def stepClause(s: Sexp): Vector[Lit] = s match
      case Ctor("input", Vector(c)) => items(c).map(lit)
      case Ctor("lemma", Vector(c)) => items(c).map(lit)
      case Ctor("derived", Vector(c, _)) => items(c).map(lit)
      case _ => throw Malformed(s"not a step: ${s.toString.take(100)}")

    def write(): String =
      declarations()
      for (a, i) <- asserts.zipWithIndex do
        val t = sTerm(a)
        val p = s"@a$i"
        out ++= s"(assume $p $t)\n"
        assumed(a) = p
      for ((s, j), k) <- steps.zip(justs).zipWithIndex do
        val c = stepClause(s)
        val p = (s, j) match
          case (Ctor("input", _), Ctor("input", Vector(origin))) => input(c, origin)
          case (Ctor("lemma", _), Ctor("uf", Vector(proof, cert))) => ufLemma(c, items(proof), cert)
          case (Ctor("lemma", _), Ctor(name, _)) => throw Unsupported(s"$name lemma")
          case (Ctor("derived", Vector(_, hints)), _) => derived(c, ints(hints))
          case _ => throw Malformed(s"step $k does not match its justification")
        proved += p -> c
      if proved.lift(conclusion).forall(_._2.nonEmpty) then throw Malformed("the conclusion is not the empty clause")
      out.result()

    // ----------------------------------------------------------------------
    // Inputs

    private def input(c: Vector[Lit], origin: Sexp): String = origin match
      case Ctor("assertion", Vector(f)) => assertion(c, int(f))
      case Ctor("definition", _) => definition(c)
      case Ctor(name, _) => throw Unsupported(s"$name input")

    /** The clause of the lowered input `f`, from the surface assertion it
     *  lowers: the same CPC term once Ethos reads the chains of the surface,
     *  then the literal of the clause that stands for it.
     */
    private def assertion(c: Vector[Lit], f: Int): String =
      val j = inputs.indexOf(f)
      if j < 0 then throw Unsupported("assertion under a level")
      val a = inputWhys.lift(j) match
        case Some(Ctor("assert", Vector(a))) => int(a)
        case other => throw Unsupported(s"input of reason ${other.getOrElse("none")}")
      val lowered = step(lTerm(f), "vik_lower", Seq(assumed(a)), Seq(lTerm(f)))
      c match
        case Vector(l) if denotes(l, f) =>
          bridge(f, l) match
            case None => lowered
            case Some(e) => step(litTerm(l), "eq_resolve", Seq(lowered, e))
        case _ => throw Unsupported("assertion with other literals")

    /** Whether literal `l` stands for the formula of node `f`, as the encoder
     *  reads formulas: through negations, implications and reflexive
     *  equalities (`denotes` in `Vik/Cert/Inputs.lean`).
     */
    private def denotes(l: Lit, f: Int, fuel: Int = 64): Boolean =
      fuel > 0 && {
        val plain = l.pos && meaning.lift(l.v).flatten.contains(f)
        lNodes(f) match
          case Ctor("not", Vector(g)) => denotes(l.neg, int(g), fuel - 1)
          case Ctor("tt", _) => l == trueLit
          case Ctor("ff", _) => l == trueLit.neg
          case Ctor("implies", Vector(a, b)) =>
            l.pos && meaning.lift(l.v).flatten.exists { m =>
              lNodes(m) match
                case Ctor("or", Vector(Lst(Vector(n, b2)))) => b2 == b && lNodes(int(n)) == Lst(Vector(Atom("not"), a))
                case _ => false
            }
          case Ctor("eq", Vector(a, b)) if a == b => l == trueLit
          case _ => plain
      }

    /** A proof that the formula of node `f` is the term of the literal `l`
     *  that stands for it, or none when they are the same term.
     */
    private def bridge(f: Int, l: Lit): Option[String] =
      val tf = lTerm(f)
      val tl = litTerm(l)
      lNodes(f) match
        case Ctor("not", Vector(g0)) =>
          val g = int(g0)
          // `(not g)` is `(not l̄)`, l̄ the term of `l.neg`.
          val inner = bridge(g, l.neg).map(b => step(s"(= $tf (not ${litTerm(l.neg)}))", "cong", Seq(b), Seq(tf)))
          if !l.pos then inner
          else
            // `l.neg` is `(not l)`: `(not (not l))` is `l`.
            val dn = step(s"(= (not (not $tl)) $tl)", "bool-double-not-elim", args = Seq(tl))
            Some(inner.fold(dn)(p => step(s"(= $tf $tl)", "trans", Seq(p, dn))))
        case Ctor("ff", _) =>
          val p = step("(= (not true) false)", "evaluate", args = Seq("(not true)"))
          Some(step("(= false (not true))", "symm", Seq(p)))
        case Ctor("implies", Vector(a, b)) =>
          Some(step(s"(= $tf $tl)", "bool-impl-elim", args = Seq(lTerm(int(a)), lTerm(int(b)))))
        case Ctor("eq", Vector(a, b)) if a == b =>
          Some(step(s"(= $tf true)", "eq-refl", args = Seq(lTerm(int(a)))))
        case _ => None

    /** The literal `x` of a clause proved by `p`, replaced by `y`, given a proof
     *  `e` of `(= x y)`.
     */
    private def rewriteLit(p: String, clause: Seq[String], x: String, y: String, e: String): (String, Seq[String]) =
      val ce = step(s"(or (not (= $x $y)) (not $x) $y)", "cnf_equiv_pos1", args = Seq(s"(= $x $y)"))
      val out = clause.map(t => if t == x then y else t)
      val concl = clauseOf(out)
      (
        step(concl, "chain_m_resolution", Seq(p, ce, e), Seq(concl, "(@list true false)", s"(@list $x (= $x $y))")),
        out
      )

    private def clauseOf(ts: Seq[String]): String = ts match
      case Seq() => "false"
      case Seq(t) => t
      case _ => ts.mkString("(or ", " ", ")")

    /** A clause of the definition of a Tseitin variable, from its CNF rule. */
    private def definition(c: Vector[Lit]): String =
      if c == Vector(trueLit) && trueLit.pos then
        val r = step("(= true true)", "refl", args = Seq("true"))
        return step("true", "true_elim", Seq(r))
      // The literal of the variable defined: the first whose formula has the
      // shape of the clause.
      val found = c.iterator.flatMap(d => cnf(c, d)).nextOption()
      found.getOrElse(throw Unsupported(s"definition clause ${c.map(litTerm).mkString(" ")}"))

    private def nodeOf(v: Int): Sexp = lNodes(meaningOf(v))

    /** The CNF step for clause `c` from the definition of the variable of
     *  literal `d`, when `c` is one, with each literal of a subformula then
     *  rewritten to the term of the literal of `c` that stands for it.
     */
    private def cnf(c: Vector[Lit], d: Lit): Option[String] =
      val f = formula(d.v)
      /** The literal of `c` that stands for node `n`, positive or negated. */
      def find(n: Int, pos: Boolean): Option[Lit] = c.find(l => denotes(if pos then l else l.neg, n))
      def has(n: Int, pos: Boolean) = find(n, pos).isDefined
      // The rule, its arguments, and the literals of its conclusion: the
      // defined formula's (`None`), or a subformula's, positive or negated.
      type Shape = (String, Seq[String], Seq[(Option[Int], Boolean)])
      val self = (None, d.pos)
      def sub(n: Int, pos: Boolean) = (Some(n), pos)
      val shape: Option[Shape] = nodeOf(d.v) match
        case Ctor("and", Vector(as)) =>
          val xs = ints(as)
          if d.pos && xs.forall(has(_, false)) then Some(("cnf_and_neg", Seq(f), self +: xs.map(sub(_, false))))
          else if !d.pos then
            xs.indexWhere(has(_, true)) match
              case -1 => None
              case i => Some(("cnf_and_pos", Seq(f, i.toString), Seq(self, sub(xs(i), true))))
          else None
        case Ctor("or", Vector(as)) =>
          val xs = ints(as)
          if !d.pos && xs.forall(has(_, true)) then Some(("cnf_or_pos", Seq(f), self +: xs.map(sub(_, true))))
          else if d.pos then
            xs.indexWhere(has(_, false)) match
              case -1 => None
              case i => Some(("cnf_or_neg", Seq(f, i.toString), Seq(self, sub(xs(i), false))))
          else None
        case Ctor("iff", Vector(x0, y0)) =>
          val (x, y) = (int(x0), int(y0))
          if d.pos && has(x, false) && has(y, false) then Some(("cnf_equiv_neg2", Seq(f), Seq(self, sub(x, false), sub(y, false))))
          else if d.pos && has(x, true) && has(y, true) then Some(("cnf_equiv_neg1", Seq(f), Seq(self, sub(x, true), sub(y, true))))
          else if !d.pos && has(x, false) && has(y, true) then
            Some(("cnf_equiv_pos1", Seq(f), Seq(self, sub(x, false), sub(y, true))))
          else if !d.pos && has(x, true) && has(y, false) then
            Some(("cnf_equiv_pos2", Seq(f), Seq(self, sub(x, true), sub(y, false))))
          else None
        case Ctor("ite", Vector(x0, a0, b0)) =>
          val (x, a, b) = (int(x0), int(a0), int(b0))
          if d.pos && has(x, false) && has(a, false) then Some(("cnf_ite_neg1", Seq(f), Seq(self, sub(x, false), sub(a, false))))
          else if d.pos && has(x, true) && has(b, false) then Some(("cnf_ite_neg2", Seq(f), Seq(self, sub(x, true), sub(b, false))))
          else if !d.pos && has(x, false) && has(a, true) then
            Some(("cnf_ite_pos1", Seq(f), Seq(self, sub(x, false), sub(a, true))))
          else if !d.pos && has(x, true) && has(b, true) then
            Some(("cnf_ite_pos2", Seq(f), Seq(self, sub(x, true), sub(b, true))))
          else None
        case _ => None
      shape match
        case Some((rule, args, lits)) =>
          def term(n: Option[Int], pos: Boolean) =
            val t = n.fold(f)(lTerm)
            if pos then t else s"(not $t)"
          val terms = lits.map(term)
          var p = step(clauseOf(terms), rule, args = args)
          var clause = terms
          for (n, pos) <- lits; m <- n do
            val l = find(m, pos).get
            for e <- litBridge(m, pos, l) do
              val r = rewriteLit(p, clause, term(n, pos), litTerm(l), e)
              p = r._1
              clause = r._2
          Some(p)
        case None =>
          nodeOf(d.v) match
            case Ctor("pred", Vector(t0)) =>
              lNodes(int(t0)) match
                case Ctor("bterm", Vector(inner)) if has(int(inner), !d.pos) => Some(excludedMiddle(f))
                case _ => None
            case _ => None

    /** A proof that the literal of node `n`, `pos` or negated, is the term of
     *  the literal `l` that stands for it, or none when they are the same term.
     */
    private def litBridge(n: Int, pos: Boolean, l: Lit): Option[String] =
      if pos then bridge(n, l)
      else
        val tn = lTerm(n)
        val inner = bridge(n, l.neg).map(b => step(s"(= (not $tn) (not ${litTerm(l.neg)}))", "cong", Seq(b), Seq(s"(not $tn)")))
        if !l.pos then inner
        else
          val tl = litTerm(l)
          val dn = step(s"(= (not (not $tl)) $tl)", "bool-double-not-elim", args = Seq(tl))
          Some(inner.fold(dn)(p => step(s"(= (not $tn) $tl)", "trans", Seq(p, dn))))

    /** `(or (not F) F)`, from the scope of `F`. */
    private def excludedMiddle(f: String): String =
      val h = id("h")
      out ++= s"(assume-push $h $f)\n"
      val p = id("p")
      out ++= s"(step-pop $p (=> $f $f) :rule scope :premises ($h))\n"
      step(s"(or (not $f) $f)", "implies_elim", Seq(p))

    // ----------------------------------------------------------------------
    // Lemmas of uninterpreted functions

    /** The term of a class: the lowered node it is. */
    private def cls(x: Int): String = lTerm(x)

    private def eq(a: Int, b: Int): String = s"(= ${cls(a)} ${cls(b)})"

    /** The lemma clause `c`, from a scope over the negation of each literal in
     *  which the equality proof derives `false`.
     */
    private def ufLemma(c: Vector[Lit], proof: Vector[Sexp], cert: Sexp): String =
      val hyps = c.map(_.neg)
      val hypSteps = hyps.map { h =>
        val p = id("h")
        out ++= s"(assume-push $p ${litTerm(h)})\n"
        h -> p
      }
      def hypStep(r: Lit): String =
        hypSteps.collectFirst { case (`r`, p) => p }.getOrElse(throw Malformed(s"no hypothesis $r"))

      /** The proof of `a = b`, `a` and `b` the classes of an atom, from the
       *  hypothesis that is its variable, positive or negated.
       */
      def fromHyp(a: Int, b: Int, r: Lit): String =
        val (x, y) = ufAtoms.lift(r.v).flatten.getOrElse(throw Malformed(s"variable ${r.v} is no atom"))
        val h = hypStep(r)
        lNodes(meaningOf(r.v)) match
          case Ctor("eq" | "iff", Vector(m1, m2)) if r.pos =>
            val (n1, n2) = (int(m1), int(m2))
            if (a, b) == (n1, n2) then h
            else if (a, b) == (n2, n1) then step(eq(a, b), "symm", Seq(h))
            else throw Unsupported(s"atom ${(x, y)} is not its formula's equality")
          case _ if y == trueClass =>
            // A Boolean term as a formula: `x = true` when it holds, `x = false`
            // when it does not.
            val (rhs, rule) = if r.pos then (trueClass, "true_intro") else (falseClass, "false_intro")
            val p = step(eq(x, rhs), rule, Seq(h))
            if (a, b) == (x, rhs) then p else step(eq(a, b), "symm", Seq(p))
          case other => throw Unsupported(s"atom of $other")

      val done = ArrayBuffer.empty[(Int, Int, String)]
      for s <- proof do
        val proved = s match
          case Ctor("refl", Vector(a0)) =>
            val a = int(a0)
            (a, a, step(eq(a, a), "refl", args = Seq(cls(a))))
          case Ctor("given", Vector(a0, b0, r)) =>
            val (a, b) = (int(a0), int(b0))
            (a, b, fromHyp(a, b, lit(r)))
          case Ctor("symm", Vector(i)) =>
            val (a, b, p) = done(int(i))
            (b, a, step(eq(b, a), "symm", Seq(p)))
          case Ctor("trans", Vector(i, j)) =>
            val (a, _, p) = done(int(i))
            val (_, d, q) = done(int(j))
            (a, d, step(eq(a, d), "trans", Seq(p, q)))
          case Ctor("cong", Vector(a0, b0, f0, args0)) =>
            val (a, b) = (int(a0), int(b0))
            val (fa, fb, pf) = done(int(f0))
            val argProofs = ints(args0).map(done(_)._3)
            val p =
              if fa == fb then step(eq(a, b), "cong", argProofs, Seq(cls(a)))
              else step(eq(a, b), "ho_cong", pf +: argProofs)
            (a, b, p)
          case Ctor(name, _) => throw Unsupported(s"equality step $name")
        done += proved

      val bottom = cert match
        case Ctor("disequal", Vector(p0, r0)) =>
          val (a, b, p) = done(int(p0))
          val r = lit(r0)
          if r == trueLit then
            // `true = false`.
            val tf = if (a, b) == (trueClass, falseClass) then p else step(eq(trueClass, falseClass), "symm", Seq(p))
            val ft = step(eq(falseClass, trueClass), "symm", Seq(tf))
            step("false", "true_elim", Seq(ft))
          else
            val (x, y) = ufAtoms(r.v).get
            val h = hypStep(r)
            lNodes(meaningOf(r.v)) match
              case Ctor("eq" | "iff", Vector(m1, m2)) =>
                val (n1, n2) = (int(m1), int(m2))
                val q = if (a, b) == (n1, n2) then p else step(eq(n1, n2), "symm", Seq(p))
                step("false", "contra", Seq(q, h))
              case _ if y == trueClass =>
                val q = if (a, b) == (x, trueClass) then p else step(eq(x, trueClass), "symm", Seq(p))
                step("false", "contra", Seq(step(formula(r.v), "true_elim", Seq(q)), h))
              case other => throw Unsupported(s"atom of $other")
        case Ctor("equal", Vector(p0)) =>
          // The first literal holds, against the hypothesis of its negation.
          val (a, b, p) = done(int(p0))
          val l = c.head
          val h = hypStep(l.neg)
          lNodes(meaningOf(l.v)) match
            case Ctor("eq" | "iff", Vector(m1, m2)) =>
              val (n1, n2) = (int(m1), int(m2))
              val q = if (a, b) == (n1, n2) then p else step(eq(n1, n2), "symm", Seq(p))
              step("false", "contra", Seq(q, h))
            case _ =>
              val (x, _) = ufAtoms(l.v).get
              val q = if (a, b) == (x, trueClass) then p else step(eq(x, trueClass), "symm", Seq(p))
              step("false", "contra", Seq(step(formula(l.v), "true_elim", Seq(q)), h))
        case Ctor(name, _) => throw Unsupported(s"$name certificate")

      // Discharge the hypotheses, innermost first.
      var body = bottom
      var concl = "false"
      for (h, _) <- hypSteps.reverse do
        concl = s"(=> ${litTerm(h)} $concl)"
        val p = id("p")
        out ++= s"(step-pop $p $concl :rule scope :premises ($body))\n"
        body = p
      if hyps.isEmpty then return bottom
      val hypTerms = hyps.map(litTerm)
      val negAnd = if hyps.length == 1 then s"(not ${hypTerms.head})" else hypTerms.mkString("(not (and ", " ", "))")
      val scoped = step(negAnd, "process_scope", Seq(body), Seq("false"))
      // `(or (not H₁) … (not Hₙ))`, then each `(not (not F))` resolved to `F`.
      val split =
        if hyps.length == 1 then scoped
        else step(hypTerms.map(h => s"(not $h)").mkString("(or ", " ", ")"), "not_and", Seq(scoped))
      val doubles = c.filter(_.pos)
      if doubles.isEmpty then split
      else
        val premises = split +: doubles.map(l => excludedMiddle(formula(l.v)))
        // In the clause, `(not (not F))`: the pivot `(not F)` is negative.
        step(
          clauseTerm(c),
          "chain_m_resolution",
          premises,
          Seq(
            clauseTerm(c),
            doubles.map(_ => "false").mkString("(@list ", " ", ")"),
            doubles.map(l => s"(not ${formula(l.v)})").mkString("(@list ", " ", ")")
          )
        )

    // ----------------------------------------------------------------------
    // Derived clauses

    /** The clause `c` by reverse unit propagation over `hints`: with every
     *  literal of `c` false, the hints in turn propagate until one is false.
     *  The resolution chain starts from that conflict and resolves, in reverse,
     *  each propagated literal it contains with the hint that propagated it.
     */
    private def derived(c: Vector[Lit], hints: Seq[Int]): String =
      // The value of each formula, by its node: literals of the same formula
      // are the same in CPC.
      def key(l: Lit): String = formula(l.v)
      val value = HashMap.empty[String, Boolean]
      def holds(l: Lit): Option[Boolean] = value.get(key(l)).map(_ == l.pos)
      for l <- c do value(key(l)) = !l.pos
      val reasons = ArrayBuffer.empty[(Lit, Int)]
      var conflict = -1
      val it = hints.iterator
      while conflict < 0 && it.hasNext do
        val h = it.next()
        val clause = proved(h)._2
        if !clause.exists(holds(_).contains(true)) then
          clause.filter(holds(_).isEmpty).distinctBy(l => (key(l), l.pos)) match
            case Vector() => conflict = h
            case Vector(u) =>
              value(key(u)) = u.pos
              reasons += u -> h
            case _ => ()
      if conflict < 0 then throw Malformed(s"hints ${hints.mkString(" ")} do not refute ${c.map(litTerm).mkString(" ")}")
      // Resolve backwards from the conflict.
      val premises = ArrayBuffer(proved(conflict)._1)
      val pols = ArrayBuffer.empty[String]
      val pivots = ArrayBuffer.empty[String]
      val current = HashSet.from(proved(conflict)._2.map(l => (key(l), l.pos)))
      for (u, h) <- reasons.reverseIterator do
        val m = key(u)
        if current.contains((m, !u.pos)) then
          current -= ((m, !u.pos))
          for l <- proved(h)._2 if !(key(l) == m && l.pos == u.pos) do current += ((key(l), l.pos))
          premises += proved(h)._1
          // The pivot as it occurs in the accumulated clause: `¬u`.
          pols += (if u.pos then "false" else "true")
          pivots += m
      val concl = clauseTerm(c)
      if premises.length == 1 then
        // The conflict clause is the clause itself, up to order.
        if proved(conflict)._2 == c then premises.head
        else step(concl, "reordering", premises.toSeq)
      else
        step(
          concl,
          "chain_m_resolution",
          premises.toSeq,
          Seq(concl, pols.mkString("(@list ", " ", ")"), pivots.mkString("(@list ", " ", ")"))
        )
