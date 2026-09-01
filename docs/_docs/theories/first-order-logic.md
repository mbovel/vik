---
title: First-order logic
extraJS:
  - js/snippets.js
extraCSS:
  - css/site.css
  - css/snippets.css
---

# First-order logic

Every question is a formula: the connectives over the atoms of the theories. VIK decides the propositional
structure with a CDCL(T) search, and the atoms with the theories described in the other pages.

**Decision procedure: yes.** CDCL is complete for the propositional structure; a question is decided
whenever its theory atoms are.

## What you can write

`and`, `or`, `not`, `=>`, `xor`, `=` between formulas, `ite`, and `let` for sharing; `true` and `false`;
uninterpreted predicates, which are functions into `Bool`.

```smt2
(declare-const p Bool)
(declare-const q Bool)
(declare-fun r (Bool) Bool)
(assert (=> p q))
(assert (or (not q) (r p)))
(assert (not (r (and p p))))
(check-sat)
```

A formula may appear where a term is expected — as an argument, in an `ite`, as a field of a datatype, or as
the body of a lambda — and two formulas with the same truth value are then the same value:

```smt2
(declare-fun f (Bool) Int)
(declare-const p Bool)
(declare-const q Bool)
(assert (= p q))
(assert (not (= (f p) (f q))))
(check-sat)
```

## Internals

The formulas are put in clauses by a Tseitin encoding: every subformula gets a variable, defined by clauses,
so the size stays linear and the sharing of `let` survives. Each atom — an equality, a bound, a tester —
becomes a variable of the SAT solver and an atom of the theory that owns it.

The search is [CDCL(T)](../components/cdclt.md) as Nieuwenhuis, Oliveras and Tinelli describe it: the SAT
solver decides and propagates, the theories are told every literal in trail order, and they answer with a
conflict (a set of literals that cannot hold together, learned as a clause) or with the literals they imply.
The solver has two watched literals per clause, first-UIP learning, VMTF decisions, phase saving and Luby
restarts, and it asks the theories for a final check when every variable is assigned.

Before that, a [simplifier](../components/vik.md) eliminates the constants that are local to a
conjunction — `c = t ∧ φ` becomes `φ[t/c]` when `c` occurs nowhere else and the conjunction is in a positive
position. That is what turns the `eq_diamond` family from an exponential path enumeration into a single
equality, and it is polarity-aware, because under a negation the conjunction is not an existential context.
