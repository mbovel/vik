---
title: Arrays
extraJS:
  - js/snippets.js
extraCSS:
  - css/site.css
---

# Arrays

Extensional arrays: `select`, `store`, equality between arrays, constant arrays and the maps of arrays.

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

## Constant arrays and maps

`((as const (Array I E)) v)` is the array that holds `v` at every index, and `((_ map f) a b …)` the array
that holds, at every index, `f` of what `a`, `b`, … hold there, for any `f` a term can apply: a connective,
an arithmetic operation, `ite`, a declared or defined function. Inox writes sets as arrays of Booleans and
bags as arrays of integers, and combines them with maps: the union of two sets is `((_ map or) s t)`.

```smt2
(set-logic QF_ALIA)
(declare-const s (Array Int Bool))
(declare-const t (Array Int Bool))
(declare-const x Int)
(assert (= s (store ((as const (Array Int Bool)) false) 1 true)))
(assert (select ((_ map or) s t) x))
(assert (not (select t x)))
(assert (not (= x 1)))
(check-sat)
```

A constant array and a map are symbols of the lowering's own, and the array lemmas instantiate what they
hold at every index of their sort and of their arrays', and at one more index, beyond the writes, which
settles what they hold away from the indices read. Over a finite index sort — `Bool`, a bit-vector, a
datatype without a field of an infinite sort — the writes may fill the sort, and the arrays are then read
at every value of it as well. An operator that reads or writes an array at an index one of the elements is
in is refused: its instances need not end.

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
