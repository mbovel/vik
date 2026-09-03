---
title: Uninterpreted functions
extraJS:
  - js/snippets.js
extraCSS:
  - css/site.css
---

# Uninterpreted functions

Sorts and functions you declare, of which the solver assumes nothing but congruence: equal arguments give
equal results.

**Decision procedure: yes.** Congruence closure decides `QF_UF`.

## What you can write

```smt2
(declare-sort U 0)
(declare-fun f (U U) U)
(declare-const a U)
(declare-const b U)
(assert (= a b))
(assert (not (= (f a b) (f b a))))
(check-sat)
```

Disequalities, `distinct`, and functions of any arity.

```smt2
(declare-sort U 0)
(declare-fun f (U) U)
(declare-const a U)
(declare-const b U)
(declare-const c U)
(assert (distinct a b c))
(assert (= (f a) (f b)))
(assert (= (f b) (f c)))
(check-sat)
```

Cardinality is not decided: an uninterpreted sort is infinite here, so a script that needs "there are at most
three values of `U`" needs to say it.

## Internals

The theory is an [e-graph](../components/egraph.md) — a union-find over the terms with a congruence table over
the applications. A true equality atom merges two classes, a false one is recorded as a disequality on the
classes, and the merge that would join two disequal classes is the conflict. The explanation of a conflict is
read from the union tree: the equalities on the path between the two terms, and, for a congruence edge, the
equalities of the arguments, recursively.

The rebuild is deferred: merges are collected and the congruence is restored once, at the theory's
`propagate`, which is exactly when the SAT solver stops propagating. What the theory then implies is every
unassigned equality atom whose sides have become equal, or disequal — found by a scan of the atoms, which
measured as fast as the event-driven bookkeeping it replaced and is fifteen lines instead of a hundred.

The e-graph is backtrackable: `push` and `pop` frame what a decision level did, and undoing a merge is
undoing the pointer changes it made, in the reverse order. That is what makes the theory usable inside a
search that changes its mind, and what makes `push`/`pop` cheap for the caller.
