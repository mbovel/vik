# Plan: algebraic data types through e-graph hooks

Status: implemented on 2026-08-29 (`egraph.Rules`, `smt.AdtRules`, the exhaustiveness clauses of the encoder), with two
departures from the plan below: a rewrite that identifies a term with an existing class goes through a derived merge
with a rule justification (an `added` hook), never a silent substitution, so that it can be explained and undone; and
testers need no lemma mechanism, since the encoder asserts the exhaustiveness clauses eagerly for every datatype term
(and pre-expands the selector terms of finite sorts). Fields of sort `Int` work through the UF–LIA combination of the encoder (shared integer terms, an equality atom per
pair); `Bool` fields are refused.

## What the theory has to decide

For datatypes declared as `(declare-datatype List ((nil) (cons (head Int) (tail List))))`:

1. **Injectivity**: `cons(x, xs) = cons(y, ys)` implies `x = y` and `xs = ys`.
2. **Distinctness**: `nil ≠ cons(x, xs)`: two different constructors never build the same value.
3. **Selectors**: `head(cons(x, xs)) = x`; on a value of another constructor a selector is unconstrained
   (SMT-LIB leaves it uninterpreted).
4. **Testers**: `(_ is cons) t` holds iff `t = cons(head(t), tail(t))`; every value is built by exactly one
   constructor (exhaustiveness).
5. **Acyclicity**: `xs ≠ cons(x, xs)`: no value contains itself (finite trees).

Barrett, Shikanian and Tinelli (2007) give the decision procedure; cvc5's `TheoryDatatypes` and Reynolds and
Blanchette (2017) the modern shape. Rules 1–4 are local to the congruence closure; 5 needs a global check.

## Hooks in the e-graph

The e-graph of `dotty`'s qualified types (`qualified_types/EGraph.scala` on the `refinement-types` branch) does all
its theory work with two hooks: **normalization on insertion** (`canonicalize` rewrites a node before it is
interned: constant folding, `And`/`Or` with `true`/`false`, sums reassociated, a `Select` on an applied constructor
replaced by the field) and **callbacks on merge** (after `merge(a, b)`, the old representative is inspected: an `And`
merged with `true` merges its conjuncts with `true`, an `Equal` merged with `true` merges its sides), with `repair`
running the congruence closure to a fixpoint. The same shape fits VIK's e-graph, with two differences to respect:
merges are undone by `pop`, and every merge needs an explanation.

Proposed interface, a `Rules` object given to `EGraph` at construction (`NoRules` by default):

```scala
trait Rules:
  /** Rewrites an application before it is added: the class it is equal to, or the
   *  application to add. `head(cons(x, xs))` becomes the class of `x`.
   */
  def normalize(eg: EGraph, app: App): EClassId | App = app

  /** Called by `rebuild` for every merge done since the last rebuild, with the
   *  two former representatives. May call `eg.merge`, `eg.disequate` and
   *  `eg.rule(...)`; the merges it causes are processed by the same rebuild.
   */
  def merged(eg: EGraph, a: EClassId, b: EClassId): Unit = ()

  /** Called by the theory's final check: a global condition on the closure. */
  def finalCheck(eg: EGraph): Option[Conflict] = None
```

- **Normalization** runs in `add`, on the canonical arguments. It returns an existing class when the application
  simplifies away (`head(cons(x, xs))` → the class of `x`; `bvnot(bvnot(t))` → `t`; `x + 0` → `x` for lowered
  integers) and the application itself otherwise. Explanations are unaffected: the caller asserted the simplified
  fact, and the justification of a normalized-away application is the rule itself (`Justification.Rule(name)`),
  with no premises. A normalization must be a *congruence*: if it fires on canonical arguments it must fire on any
  arguments equal to them, otherwise a later merge would need a re-normalization; `rebuild` therefore re-runs
  `normalize` on the nodes it re-canonicalizes (the `pending` list), which is where the deferred rebuild helps: the
  dotty e-graph does the same in `repair`.
- **Merge callbacks** run in `rebuild`, after the union, on the recorded pairs (the e-graph already records what it
  needs for `pop`; the pairs are those `Undo.Merged` records, or a small list kept between rebuilds). The callback
  sees both classes' nodes (the class member list is there: `next`) and asserts consequences with justifications:
  - injectivity: a class holding `cons(x, xs)` and `cons(y, ys)` → `merge(x, y, Rule("injective", cons(x,xs) = cons(y,ys)))`,
    same for the second argument;
  - distinctness: a class holding `nil` and `cons(..)` → a conflict, as a disequality violated with the rule as reason;
  - selectors: a class that gains a constructor node `cons(x, xs)` while `head(t)` exists for a `t` in the class →
    `merge(head(t), x, Rule("selector", t = cons(x, xs)))`; the symmetric case (a selector application added to a
    class that already holds a constructor) is the normalization above;
  - testers: `is_cons(t)` merged with `true` → `merge(t, cons(head(t), tail(t)))` (adding the applications), merged
    with `false` → `disequate(t, cons(head(t), tail(t)))` is not enough (it says nothing about other constructors);
    exhaustiveness `is_nil(t) ∨ is_cons(t)` is a clause for the SAT solver, as in cvc5 (splitting on demand: the
    theory returns lemmas), so testers need the `Theory` interface to grow a `lemmas` result.
- **Explanations**: a new `Justification.Rule(name: String, premises: Array[(EClassId, EClassId)])` next to
  `Asserted` and `Congruence`: `explain` recurses into the premises exactly as it does for the two applications of a
  congruence. A conflict raised by a rule carries its premises the same way.
- **Undo**: rule-derived merges and disequalities go through `merge`/`disequate`, hence through the trail; nothing
  else to undo. A normalization that returned an existing class did not create a node; if it created applications
  (`cons(head(t), tail(t))`), `add` recorded them.
- **Acyclicity** is `finalCheck`: a depth-first search over the constructor edges between classes (a class with a
  constructor node points to the classes of its arguments); a cycle is a conflict explained by the equalities that
  made each edge point where it does (the union-tree explanations of the argument classes along the cycle).

## Why hooks rather than a separate theory

A separate `AdtTheory` next to `UfTheory` would have to mirror the classes of the e-graph to see constructor nodes
merge — the exchange-of-equalities problem that QF_UFLIA also has. Rules inside the e-graph see every merge as it
happens and reuse the trail and the explanations. The price is an e-graph API that knows about rules (three methods
and one justification case, an estimated 60 lines), which the LIA lowering of bit-vectors and integer constant
folding can use as well.

## Steps

1. `Justification.Rule` and `explain` for it; `Rules`/`NoRules` with `normalize` and `merged`; `rebuild` calling
   them; tests with a toy rule (idempotence `f(f(x)) = f(x)`).
2. Datatype declarations reaching the e-graph: `smt`'s encoder registers constructors, selectors and testers with an
   `AdtRules` instance built from the `Datatype` declarations (`smt2` already produces them).
3. Injectivity, distinctness and selectors; the differential test against brute force over small finite datatypes.
4. Testers with lemmas (`Theory.Result.Ok` gains lemma clauses; the CDCL(T) solver adds them) and exhaustiveness.
5. Acyclicity in `finalCheck`; QF_DT instances from SMT-LIB in the benchmarks.
6. Then the same hooks for integer constant folding in the lowered bit-vector scripts (measured, kept if useful).
