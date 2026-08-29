package net.bovel.bench

import scala.collection.mutable.HashMap

import com.microsoft.z3.{BoolExpr, Constructor, Context, DatatypeSort, Expr, FuncDecl, IntExpr, Sort}
import net.bovel.smt.{Formula, Term}
import net.bovel.smt2.{Command, Script}

/** Translates a parsed [[Script]] straight into z3's AST through its Java API,
 *  declarations included, the way a program using z3 in process would build its
 *  query (no SMT-LIB text in between). `Int` covers the lowered bit-vectors as
 *  well.
 */
final class Z3Encoder(ctx: Context):
  private val sorts = HashMap.empty[String, Sort]
  private val decls = HashMap.empty[String, FuncDecl[?]]

  private def sort(name: String): Sort = name match
    case "Bool" => ctx.getBoolSort
    case "Int" => ctx.getIntSort
    case _ => sorts.getOrElseUpdate(name, ctx.mkUninterpretedSort(name))

  /** Declares what the script declares and returns its assertions. */
  def assertions(script: Script): List[BoolExpr] =
    script.commands.flatMap {
      case Command.DeclareSort(name, _) =>
        sort(name)
        Nil
      case Command.DeclareFun(name, args, result) =>
        decls(name) = ctx.mkFuncDecl(name, args.map(sort).toArray, sort(result))
        Nil
      case Command.DeclareDatatypes(datatypes) =>
        // Mutually recursive datatypes: field sorts naming a datatype of the group are references by index.
        val names = datatypes.map(_.name)
        val constructors = datatypes.map { d =>
          d.constructors.map { c =>
            val fieldSorts = c.selectors.map((_, s) => if names.contains(s) then null else sort(s))
            val refs = c.selectors.map((_, s) => names.indexOf(s))
            ctx.mkConstructor(c.name, c.tester, c.selectors.map(_._1).toArray, fieldSorts.toArray, refs.toArray)
          }.toArray
        }
        val sorts = ctx.mkDatatypeSorts(names.toArray, constructors.toArray)
        for (d, i) <- datatypes.zipWithIndex do
          this.sorts(d.name) = sorts(i)
          for (c, k) <- d.constructors.zipWithIndex do
            val constructor = constructors(i)(k)
            decls(c.name) = constructor.ConstructorDecl()
            decls(c.tester) = constructor.getTesterDecl
            for ((selector, _), j) <- c.selectors.zipWithIndex do decls(selector) = constructor.getAccessorDecls()(j)
        Nil
      case Command.Assert(formula) => List(bool(formula))
      case _ => Nil
    }

  def bool(f: Formula): BoolExpr = f match
    case Formula.True => ctx.mkTrue()
    case Formula.False => ctx.mkFalse()
    case Formula.Not(a) => ctx.mkNot(bool(a))
    case Formula.And(as) => ctx.mkAnd(as.map(bool)*)
    case Formula.Or(as) => ctx.mkOr(as.map(bool)*)
    case Formula.Implies(a, b) => ctx.mkImplies(bool(a), bool(b))
    case Formula.Iff(a, b) => ctx.mkIff(bool(a), bool(b))
    case Formula.Ite(c, t, e) => ctx.mkITE(bool(c), bool(t), bool(e)).asInstanceOf[BoolExpr]
    case Formula.Eq(l, r) => ctx.mkEq(term(l), term(r))
    case Formula.Pred(t) => term(t).asInstanceOf[BoolExpr]
    case Formula.Le(l, r) => ctx.mkLe(int(l), int(r))

  def term(t: Term): Expr[?] = t match
    case Term.Const(name) => ctx.mkConst(decls(name))
    case Term.App(Term.Const(name), args) => ctx.mkApp(decls(name), args.map(term)*)
    case Term.App(fun, _) => throw IllegalArgumentException(s"higher-order application $fun")
    case Term.Num(v) => ctx.mkInt(v.toString)
    case Term.Linear(terms, c) =>
      val summands = terms.map((a, u) => if a == 1 then int(u) else ctx.mkMul(ctx.mkInt(a.toString), int(u))) ++
        Option.when(c.signum != 0)(ctx.mkInt(c.toString))
      ctx.mkAdd(summands*)
    case Term.Lambda(_, _) | Term.Var(_) => throw UnsupportedOperationException("lambdas are not encoded for z3")

  private def int(t: Term): IntExpr = term(t).asInstanceOf[IntExpr]
