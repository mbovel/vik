---
title: Non-features
extraCSS:
  - css/site.css
---

# Non-features

What VIK does not do, on purpose or not yet, and what happens when a script asks for it: the front end and
the lowering refuse it with a message, before any search — a question is never answered on a meaning the
solver does not implement. The theory pages say what is in; this page says what is out, and why when the
reason is a decision rather than a gap.

## Out by decision

**Functions as values.** SMT-LIB 2.7's higher-order fragment — function sorts `(-> S T)`, `lambda`,
partial application, equality between functions — was in the solver until September 2026 and was removed.
The implementation was a lambda as an e-graph node over its body, beta-reduction at construction, and two
lemma schemas, one applying a lambda wherever a function could become equal to it and one giving each
function equality a witness. Three scripts of four lines each showed what that amounts to: the application
lemmas did not terminate on a composition (`g = λx. f (f x)` with `f` defined and `g` applied); two
different lambdas equated through a constant were accepted as `sat`, which is wrong; and a pipeline of
lambdas chosen by disjunctions went from a second at seven stages to no answer at eight. Deciding that two
lambdas are the same function is a universal statement over their bodies, which is not the kind of question
this solver is built to decide (cvc5 answers `unknown` on the same equalities). What a client needs from
lambdas belongs to the client: a closure applied to known arguments is a substitution on the client's own
trees, and an unknown function value applied to arguments is an uninterpreted function once function values
are defunctionalized — a constant of an uninterpreted sort under an `apply` symbol — as the verifiers built
on SMT solvers do. A function symbol is applied, never a value: `(= f g)` is refused.

**Bit-blasting.** Bit-vectors are integers in `[0, 2ⁿ)` and the operations that are arithmetic modulo `2ⁿ`
are lowered to linear integer arithmetic; see [bit-vectors](theories/bit-vectors.md) for the fragment. A
product, a division, a shift or a bitwise operation of two unknowns is refused. A counterexample-guided
loop that kept such operations as uninterpreted symbols and held them to their meaning by value lemmas from
the candidate models was tried and reverted the same month: it works, and it puts an evaluation loop around
the search that the solver is meant to be one instance of — a client that wants it can run it on top of the
solver's models.

**Non-linear arithmetic.** Multiplication of two unknowns and division by an unknown are refused: the
arithmetic is linear over the integers and the reals, and the reals are computed over the integers (see
[reals](theories/reals.md)).

**Quantifiers.** `forall` and `exists` are refused.

**The other theories.** Strings, regular languages, sequences, sets, bags, floating point: their sorts are
refused at the declaration, rather than read as uninterpreted sorts.

## Not yet

- `div`, `mod` and `abs` are refused; see [integers](theories/integers.md).
- Parametric datatypes (`declare-datatypes` with sort parameters) are refused; see
  [datatypes](theories/datatypes.md).
- Arrays of arrays (`store` on a nested array) are refused; see [arrays](theories/arrays.md).
- `check-sat-using`, z3's tactic form, is read as `check-sat`.
