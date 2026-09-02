---
title: Theories
extraCSS:
  - css/site.css
---

# Theories

What VIK decides, and how. Each page shows what you can write, with snippets you can run, and then what is
behind it.

- [First-order logic](first-order-logic.md): the connectives, decided by a CDCL(T) search.
- [Uninterpreted functions](uninterpreted-functions.md): declared sorts and functions, congruence closure.
- [Integer arithmetic](integers.md): linear bounds, a simplex over the integers, and the combination with
  the functions.
- [Real arithmetic](reals.md): the simplex with δ-rationals for strict bounds.
- [Algebraic datatypes](datatypes.md): constructors, selectors, testers, acyclicity.
- [Bit-vectors](bit-vectors.md): the arithmetic fragment, lowered to integers, no bit-blasting.
- [Arrays](arrays.md): `select` as application, `store` by lemmas, extensional.
- [Sort parameters](polymorphism.md): `declare-sort-parameter`, parametric datatypes, `define-sort`, `as` —
  compiled away into monomorphic instances.

Each page opens with whether VIK is a decision procedure for its fragment: yes for uninterpreted
functions, integers (branch and bound inside the small-model box), reals, datatypes, the bit-vector
fragment and flat arrays. Every answer is
within a fuel: any question can also stop at the budget with `unknown`.

Not supported: quantifiers, strings, sets, floating point. A script that uses them is
refused, not answered.
