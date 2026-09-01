---
title: Arrays
extraJS:
  - js/snippets.js
extraCSS:
  - css/site.css
---

# Arrays

Extensional arrays: `select`, `store`, and equality between arrays.

**Decision procedure: yes, for flat arrays.** The store lemmas over the finite index set decide `QF_AX`
(and its combinations, up to the integer page's caveat); `store` on a nested array — an array of arrays —
is refused up front.

## What you can write

```smt2
(set-logic QF_AX)
(declare-sort I 0)
(declare-sort E 0)
(declare-const a (Array I E))
(declare-const i I)
(declare-const j I)
(declare-const v E)
(assert (not (= (select (store a i v) j) (select a j))))
(assert (not (= i j)))
(check-sat)
```

An array is a function: `(Array I E)` is the function sort `(-> I E)`, and `select` is application. That
gives congruence — equal arrays select equal values — and extensionality — distinct arrays differ at a
witness index — from the [function machinery](lambdas.md), with nothing added.

`store` is the one new operation. Each `(store a i v)` is a fresh array constrained by two lemmas: the
write is read back (`(select (store a i v) i)` is `v`), and at every other index the store reads as `a` —
one clause per index term of the sort, `i = j ∨ select(s, j) = select(a, j)`, so the SAT solver decides
where indices collide. The index set is the finite set of indices written in the problem, extensionality
witnesses included.

Arrays combine with the other theories through their indices and elements: `(Array Int Int)` shares its
terms with [integer arithmetic](integers.md) like any function over integers. `store` on a nested array
(an array of arrays) is not supported; models do not value arrays yet.
