package net.bovel.bench

import scala.collection.mutable.{ArrayBuffer, HashMap}

import ap.api.SimpleAPI
import ap.basetypes.IdealInt
import ap.parser.{IAtom, IExpression, IFormula, IFormulaITE, IFunApp, IFunction, ITerm, ITermITE}
import ap.theories.ADT
import ap.theories.rationals.Rationals
import ap.types.Sort
import net.bovel.vik.{Command, Script}
import net.bovel.smt2.Datatype
import net.bovel.vik.{Formula, Term}

/** An instance compiled to a straight-line program of princess API calls: what
 *  a verifier that builds princess's terms directly executes per query — the
 *  princess analogue of [[Z3Program]]. [[compile]] reads the parsed script
 *  once; [[check]] replays the program against a fresh prover: symbol
 *  creation, term construction, asserts, `push`/`pop` and every `check-sat`,
 *  nothing else. The API route also sidesteps the SMT-LIB parser's
 *  environment, which rejects a re-declaration after `pop`
 *  (external-issues.md). `Int` covers the lowered bit-vectors.
 */
final class PrincessProgram(instructions: Array[PrincessProgram.Op], slotCount: Int, declCount: Int, sortCount: Int):
  import PrincessProgram.Op

  /** Replays the program and returns the `check-sat` answers. */
  def check(): List[Boolean] =
    SimpleAPI.withProver { p =>
      import IExpression.*
      val slots = new Array[AnyRef](slotCount) // ITerm or IFormula
      val decls = new Array[AnyRef](declCount) // IFunction, or a predicate for a Bool result
      val sorts = new Array[Sort](sortCount)
      val adts = new Array[ADT](sortCount) // by the sort id of the first datatype of a group
      val answers = ArrayBuffer.empty[Boolean]
      def term(i: Int): ITerm = slots(i).asInstanceOf[ITerm]
      def rat(x: Int): ITerm = // an integer-sorted slot (a shared literal) lifted into the rationals
        val t = term(x)
        if Sort.sortOf(t) == Rationals.dom then t else Rationals.int2ring(t)
      def formula(i: Int): IFormula = slots(i).asInstanceOf[IFormula]
      for op <- instructions do
        op match
          case Op.IntSort(id) => sorts(id) = Sort.Integer
          case Op.RealSort(id) => sorts(id) = Rationals.dom
          case Op.BoolSort(id) => sorts(id) = Sort.MultipleValueBool // a Bool argument is a term here
          case Op.UninterpretedSort(id, name) => sorts(id) = Sort.createInfUninterpretedSort(name)
          case Op.DeclareAdts(datatypes, sortIds, ctorKeys, selectorKeys, testerKeys) =>
            val names = datatypes.map(_.name)
            val signatures = datatypes.map { d =>
              d.constructors.map { c =>
                val arguments = c.selectors.map { (selector, s) =>
                  val arg: ADT.CtorArgSort =
                    if names.contains(s.text) then ADT.ADTSort(names.indexOf(s.text))
                    else ADT.OtherSort(sorts(sortIds(s.text)))
                  (selector, arg)
                }
                (c.name, ADT.CtorSignature(arguments, ADT.ADTSort(names.indexOf(d.name))))
              }
            }
            val adt = new ADT(names, signatures.flatten)
            adts(sortIds(names.head)) = adt
            for (d, i) <- datatypes.zipWithIndex do sorts(sortIds(d.name)) = adt.sorts(i)
            for ((c, k), f) <- ctorKeys.zip(adt.constructors) do decls(k) = f
            for ((s, k), f) <- selectorKeys.zip(adt.selectors.flatten) do decls(k) = f
          case Op.DeclareConst(dst, name, sortId) => slots(dst) = p.createConstant(name, sorts(sortId))
          case Op.DeclareBool(dst, name) => slots(dst) = p.createBooleanVariable(name)
          case Op.DeclareFun(id, name, argSorts, result) =>
            decls(id) = p.createFunction(name, argSorts.map(sorts).toSeq, sorts(result))
          case Op.DeclareRel(id, name, arity) => decls(id) = p.createRelation(name, arity)
          case Op.MkApp(dst, decl, args) =>
            slots(dst) = decls(decl) match
              case f: IFunction => IFunApp(f, args.map(term).toSeq)
              case r: ap.terfor.preds.Predicate => IAtom(r, args.map(term).toSeq)
          case Op.MkIs(dst, adtSortId, ctor, arg) => slots(dst) = adts(adtSortId).hasCtor(term(arg), ctor)
          case Op.MkNum(dst, value) => slots(dst) = i(IdealInt(value.bigInteger))
          case Op.MkLinear(dst, coefficients, args, constant) =>
            var sum: ITerm = i(IdealInt(constant.bigInteger))
            for (c, a) <- coefficients.zip(args) do sum = sum + (term(a) * IdealInt(c.bigInteger))
            slots(dst) = sum
          case Op.MkLinearR(dst, coefficients, args, constant) =>
            // Rational arithmetic through the Rationals theory; integer slots are lifted where used.
            var sum: ITerm = Rationals.int2ring(i(IdealInt(constant.bigInteger)))
            for (c, a) <- coefficients.zip(args) do
              sum = Rationals.plus(sum, Rationals.times(IdealInt(c.bigInteger), rat(a)))
            slots(dst) = sum
          case Op.MkBoolArg(dst, f) => slots(dst) = ITermITE(formula(f), i(0), i(1)) // 0 is princess's true
          case Op.MkTrue(dst) => slots(dst) = i(true)
          case Op.MkFalse(dst) => slots(dst) = i(false)
          case Op.MkNot(dst, a) => slots(dst) = !formula(a)
          case Op.MkAnd(dst, args) => slots(dst) = and(args.map(formula))
          case Op.MkOr(dst, args) => slots(dst) = or(args.map(formula))
          case Op.MkImplies(dst, a, b) => slots(dst) = formula(a) ==> formula(b)
          case Op.MkIff(dst, a, b) => slots(dst) = formula(a) <=> formula(b)
          case Op.MkIte(dst, c, t, e) => slots(dst) = IFormulaITE(formula(c), formula(t), formula(e))
          case Op.MkEq(dst, l, r) => slots(dst) = term(l) === term(r)
          case Op.MkEqR(dst, l, r) => slots(dst) = rat(l) === rat(r)
          case Op.MkLe(dst, l, r) => slots(dst) = term(l) <= term(r)
          case Op.MkLeR(dst, l, r) => slots(dst) = Rationals.leq(rat(l), rat(r))
          case Op.MkLt(dst, l, r) => slots(dst) = term(l) < term(r)
          case Op.MkLtR(dst, l, r) => slots(dst) = Rationals.lt(rat(l), rat(r))
          case Op.Assert(slot) => p.addAssertion(formula(slot))
          case Op.Push(levels) => for _ <- 1 to levels do p.push()
          case Op.Pop(levels) => for _ <- 1 to levels do p.pop()
          case Op.Check => answers += (p.checkSat(true) == SimpleAPI.ProverStatus.Sat)
      answers.toList
    }

object PrincessProgram:
  enum Op:
    case IntSort(id: Int)
    case RealSort(id: Int)
    case BoolSort(id: Int)
    case UninterpretedSort(id: Int, name: String)
    case DeclareAdts(
        datatypes: List[Datatype],
        sortIds: Map[String, Int],
        ctorKeys: List[(String, Int)],
        selectorKeys: List[(String, Int)],
        testerKeys: Map[String, (Int, Int)]
    )
    case DeclareConst(dst: Int, name: String, sortId: Int)
    case DeclareBool(dst: Int, name: String)
    case DeclareFun(id: Int, name: String, argSorts: Array[Int], result: Int)
    case DeclareRel(id: Int, name: String, arity: Int)
    case MkApp(dst: Int, decl: Int, args: Array[Int])
    case MkIs(dst: Int, adtSortId: Int, ctor: Int, arg: Int)
    case MkNum(dst: Int, value: BigInt)
    case MkLinear(dst: Int, coefficients: Array[BigInt], args: Array[Int], constant: BigInt)
    case MkLinearR(dst: Int, coefficients: Array[BigInt], args: Array[Int], constant: BigInt)
    case MkBoolArg(dst: Int, formula: Int)
    case MkTrue(dst: Int)
    case MkFalse(dst: Int)
    case MkNot(dst: Int, a: Int)
    case MkAnd(dst: Int, args: Array[Int])
    case MkOr(dst: Int, args: Array[Int])
    case MkImplies(dst: Int, a: Int, b: Int)
    case MkIff(dst: Int, a: Int, b: Int)
    case MkIte(dst: Int, c: Int, t: Int, e: Int)
    case MkEq(dst: Int, l: Int, r: Int)
    case MkEqR(dst: Int, l: Int, r: Int)
    case MkLe(dst: Int, l: Int, r: Int)
    case MkLeR(dst: Int, l: Int, r: Int)
    case MkLt(dst: Int, l: Int, r: Int)
    case MkLtR(dst: Int, l: Int, r: Int)
    case Assert(slot: Int)
    case Push(levels: Int)
    case Pop(levels: Int)
    case Check

  /** Compiles the script: the only walk of VIK's tree, done before measuring. */
  def compile(script: Script): PrincessProgram =
    val out = ArrayBuffer.empty[Op]
    val sortIds = HashMap.empty[String, Int]
    val declIds = HashMap.empty[String, Int]
    val slots = HashMap.empty[Formula | Term, Int]
    val resultSorts = HashMap.empty[String, String] // declared name to result sort, for realTerm
    def realTerm(t: Term): Boolean = t match
      case Term.Const(name) => resultSorts.get(name).contains("Real")
      case Term.App(Term.Const(name), _) => resultSorts.get(name).contains("Real")
      case Term.Linear(terms, _) => terms.exists((_, u) => realTerm(u))
      case _ => false
    val boolResult = HashMap.empty[String, Boolean] // functions whose result is Bool: relations
    val testers = HashMap.empty[String, (Int, Int)] // "(_ is C)" to (adt group sort id, ctor index)
    var slotCount = 0
    var declCount = 0
    def freshSlot(): Int =
      slotCount += 1
      slotCount - 1
    def freshDecl(): Int =
      declCount += 1
      declCount - 1
    def sortId(name: String): Int = sortIds.getOrElseUpdate(
      name, {
        val id = sortIds.size
        name match
          case "Int" => out += Op.IntSort(id)
          case "Real" => out += Op.RealSort(id)
          case "Bool" => out += Op.BoolSort(id)
          case _ => out += Op.UninterpretedSort(id, name)
        id
      }
    )
    def term(t: Term): Int = t match
      case Term.Bool(f) =>
        val dst = slots.getOrElseUpdate(
          t, {
            val d = freshSlot()
            out += Op.MkBoolArg(d, formula(f))
            d
          }
        )
        dst
      case _ =>
        slots.getOrElseUpdate(
          t, {
            val dst = freshSlot()
            t match
              case Term.Const(name) =>
                testers.get(name) match
                  case Some(_) => throw IllegalArgumentException(s"tester $name as a constant")
                  case None => out += Op.MkApp(dst, declIds(name), Array.empty)
              case Term.App(Term.Const(name), as) =>
                testers.get(name) match
                  case Some((adtSortId, ctor)) => out += Op.MkIs(dst, adtSortId, ctor, term(as.head))
                  case None => out += Op.MkApp(dst, declIds(name), as.map(term).toArray)
              case Term.App(fun, _) => throw IllegalArgumentException(s"higher-order application $fun")
              case Term.Num(v) => out += Op.MkNum(dst, v)
              case Term.Linear(terms, c) if terms.exists((_, u) => realTerm(u)) =>
                out += Op.MkLinearR(dst, terms.map(_._1).toArray, terms.map((_, u) => term(u)).toArray, c)
              case Term.Linear(terms, c) =>
                out += Op.MkLinear(dst, terms.map(_._1).toArray, terms.map((_, u) => term(u)).toArray, c)
              case Term.Lambda(_, _) | Term.Var(_) =>
                throw UnsupportedOperationException("lambdas are not encoded for princess")
              case Term.Bool(_) => throw IllegalStateException("handled above")
            dst
          }
        )
    def formula(f: Formula): Int = slots.getOrElseUpdate(
      f, {
        f match
          case Formula.Pred(t) => term(t) // a Bool application: its slot is the formula
          case _ =>
            val dst = freshSlot()
            f match
              case Formula.True => out += Op.MkTrue(dst)
              case Formula.False => out += Op.MkFalse(dst)
              case Formula.Not(a) => out += Op.MkNot(dst, formula(a))
              case Formula.And(as) => out += Op.MkAnd(dst, as.map(formula).toArray)
              case Formula.Or(as) => out += Op.MkOr(dst, as.map(formula).toArray)
              case Formula.Implies(a, b) => out += Op.MkImplies(dst, formula(a), formula(b))
              case Formula.Iff(a, b) => out += Op.MkIff(dst, formula(a), formula(b))
              case Formula.Ite(c, t, e) => out += Op.MkIte(dst, formula(c), formula(t), formula(e))
              case Formula.Eq(l, r) =>
                out += (if realTerm(l) || realTerm(r) then Op.MkEqR(dst, term(l), term(r))
                        else Op.MkEq(dst, term(l), term(r)))
              case Formula.Le(l, r) =>
                out += (if realTerm(l) || realTerm(r) then Op.MkLeR(dst, term(l), term(r))
                        else Op.MkLe(dst, term(l), term(r)))
              case Formula.Less(l, r) =>
                out += (if realTerm(l) || realTerm(r) then Op.MkLtR(dst, term(l), term(r))
                        else Op.MkLt(dst, term(l), term(r)))
              case Formula.Pred(_) => throw IllegalStateException("handled above")
            dst
      }
    )
    for command <- script.commands do
      command match
        case Command.DeclareSort(name, _) => sortId(name)
        case Command.DeclareFun(name, Nil, "Bool") =>
          val dst = freshSlot()
          slots(Term.Const(name)) = dst
          out += Op.DeclareBool(dst, name)
        case Command.DeclareFun(name, Nil, result) =>
          val dst = freshSlot()
          slots(Term.Const(name)) = dst
          resultSorts(name) = result
          out += Op.DeclareConst(dst, name, sortId(result))
        case Command.DeclareFun(name, args, "Bool") =>
          val id = freshDecl()
          declIds(name) = id
          boolResult(name) = true
          args.foreach(sortId) // the sorts exist; princess relations are over terms
          out += Op.DeclareRel(id, name, args.length)
        case Command.DeclareFun(name, args, result) =>
          val id = freshDecl()
          declIds(name) = id
          resultSorts(name) = result
          out += Op.DeclareFun(id, name, args.map(sortId).toArray, sortId(result))
        case Command.DeclareDatatypes(datatypes) =>
          // The datatype names take sort ids silently: DeclareAdts fills them, no uninterpreted sort op.
          for d <- datatypes do if !sortIds.contains(d.name) then sortIds(d.name) = sortIds.size
          val groupId = sortIds(datatypes.head.name)
          for d <- datatypes; c <- d.constructors; (_, s) <- c.selectors if !datatypes.exists(_.name == s.text) do
            sortId(s.text)
          val ctorKeys = ArrayBuffer.empty[(String, Int)]
          val selectorKeys = ArrayBuffer.empty[(String, Int)]
          var ctorIndex = 0
          for d <- datatypes; c <- d.constructors do
            val k = freshDecl()
            declIds(c.name) = k
            ctorKeys += ((c.name, k))
            testers(c.tester) = (groupId, ctorIndex)
            ctorIndex += 1
            for (selector, _) <- c.selectors do
              val sk = freshDecl()
              declIds(selector) = sk
              selectorKeys += ((selector, sk))
          out += Op.DeclareAdts(datatypes, sortIds.toMap, ctorKeys.toList, selectorKeys.toList, testers.toMap)
        case Command.Assert(f) => out += Op.Assert(formula(f))
        case Command.AssertNamed(_, f) => out += Op.Assert(formula(f))
        case Command.Push(levels) => out += Op.Push(levels)
        case Command.Pop(levels) => out += Op.Pop(levels)
        case Command.CheckSat => out += Op.Check
        case _ => ()
    PrincessProgram(out.toArray, slotCount, declCount, sortIds.size)
