---
title: Arrays
extraJS:
  - js/snippets.js
extraCSS:
  - css/site.css
  - css/snippets.css
---

# Arrays

Extensional arrays: `select`, `store`, and equality between arrays.

**Decision procedure: yes.** The store lemmas over the finite index set decide `QF_AX` and its combinations
(up to the integer page's caveat), arrays of arrays included.

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

An array is a function to the solver: `(Array I E)` becomes a function sort in the lowering, and `select` is
application. That
gives congruence — equal arrays select equal values — and extensionality — distinct arrays differ at a
witness index — from the machinery of [uninterpreted functions](uninterpreted-functions.md) and one
witness lemma per array equality, with nothing else added.

`store` is the one new operation. Each `(store a i v)` is a fresh array constrained by two lemmas: the
write is read back (`(select (store a i v) i)` is `v`), and at every other index the store reads as `a` —
one clause per index term of the sort, `i = j ∨ select(s, j) = select(a, j)`, so the SAT solver decides
where indices collide. The index set is the finite set of indices written in the problem, extensionality
witnesses included.

Arrays combine with the other theories through their indices and elements: `(Array Int Int)` shares its
terms with [integer arithmetic](integers.md) like any function over integers.

An array of arrays is a function of two arguments to the solver, its inner arrays functions of one; a
`store` at either level gets the lemmas of its level, an equality between inner arrays a witness of its
own, and the passes run to a fixpoint of what they emit. The price is in the indices: with integer indices
at both levels, every index of a level meets every other in an equality atom, and a matrix read at many
places costs the search a case per arrangement — seconds where a flat array takes milliseconds.

```smt2
(set-logic QF_ALIA)
(declare-const m (Array Int (Array Int Int)))
(declare-const n (Array Int (Array Int Int)))
(declare-const i Int)
(assert (= n (store m i (store (select m i) 2 7))))
(assert (= (select (select m i) 2) 3))
(assert (= (select (select n i) 2) (select (select m i) 2)))
(check-sat)
```

## Models

A model values an array by a default and the indices the script read or wrote, in SMT-LIB's own form —
`(store (store ((as const (Array Int Int)) 0) 1 10) 2 20)` — and `get-value` reads through such a value,
`store`s in the query included. Two arrays the script keeps distinct differ at the witness index the
solver introduced for them.

```smt2
(set-logic QF_ALIA)
(set-option :produce-models true)
(declare-const a (Array Int Int))
(declare-const b (Array Int Int))
(assert (= b (store a 1 10)))
(assert (= (select a 2) 20))
(check-sat)
(get-value ((select b 1) (select b 2) (select (store a 2 0) 2)))
(get-model)
```
