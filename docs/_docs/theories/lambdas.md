---
title: Functions as values
extraJS:
  - js/snippets.js
extraCSS:
  - css/site.css
  - css/snippets.css
---

# Functions as values

The core of the higher-order fragment of SMT-LIB 2.7: function sorts, lambdas, partial application, and
equality between functions.

**Decision procedure: no.** This is a sound core — beta-reduction, application lemmas and extensionality
witnesses — with no completeness claim. An equality that entangles a lambda with itself asks more than any
finite check can answer, and the solver says so:

```smt2
(set-logic HO_ALL)
(declare-const g (-> Int Int))
(assert (= (lambda ((x Int)) (g (g x))) (lambda ((x Int)) (+ x 1))))
(check-sat)
```

No total `g` over the integers satisfies `g ∘ g = successor` — `g` would have to be a bijective square
root of a single infinite cycle — yet a finite witness set looks consistent, so accepting `sat` would be
wrong. Equalities between two lambdas, or between a function and a lambda mentioning it, answer `unknown`
when the search would otherwise accept them (cvc5 answers the same); a plain definition, `g = (lambda ...)`
with `g` not in the body, keeps its answers.

## What you can write

```smt2
(declare-fun f (Int) Int)
(declare-const g (-> Int Int))
(assert (= g (lambda ((x Int)) (+ (f x) 1))))
(assert (not (= (g 2) (+ (f 2) 1))))
(check-sat)
```

Applications are curried: `(f 1 2)` and `((f 1) 2)` are the same term, and `(f 1)` is a function value.

```smt2
(declare-fun f (Int Int) Int)
(declare-const g (-> Int Int))
(assert (= g (f 1)))
(assert (not (= (g 2) (f 1 2))))
(check-sat)
```

Two lambdas with the same body are the same function, whatever the parameters are called, and functions that
differ differ somewhere — extensionality:

```smt2
(declare-fun f (Int) Int)
(assert (distinct f (lambda ((x Int)) (f x))))
(check-sat)
```

A body may be a formula, so predicates are values too:

```smt2
(declare-const positive (-> Int Bool))
(assert (= positive (lambda ((x Int)) (> x 0))))
(assert (positive 5))
(assert (not (positive 6)))
(check-sat)
```

Quantifiers are not supported: `forall` and `exists` are refused.

## Internals

Terms carry de Bruijn indices, so alpha-equivalent lambdas are literally the same term, and a lambda is a
node of the [e-graph](../components/egraph.md) over the class of its body — two lambdas with equal bodies are
then congruent, for free.

Beta-reduction happens twice. A redex that is written, or that the simplifier creates when it substitutes a
lambda for a constant, is reduced when the term is built, so terms are in normal form. A redex that only the
search creates — `f(args)` where `f` becomes equal to a lambda — is a lemma of the encoder:
`f ≠ l ∨ f(args) = l(args)`, for every closed application and closed function term of matching sorts,
repeated to a fixpoint.

Extensionality is a lemma too: for every equality between function-sorted terms in the assertions,
`f = g ∨ f(c) ≠ g(c)` with a fresh witness `c`. So `unsat` and `sat` follow the usual meaning of functions on
the equalities the script asks about.

Soundness rests on one invariant: no atom is ever put on a term with a free bound variable. The sharing of
integers and the exhaustiveness of datatypes skip open terms, `ite` and the bit-vector lowering are refused
under a lambda, and no rule concludes from the equality of two lambdas to the equality of their bodies — so
an equality the e-graph holds between open terms holds for every value of their variables.
