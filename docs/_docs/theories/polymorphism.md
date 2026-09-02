---
title: Sort parameters
extraJS:
  - js/snippets.js
extraCSS:
  - css/site.css
  - css/snippets.css
---

# Sort parameters

The polymorphism of SMT-LIB 2.7: `declare-sort-parameter`, parametric datatypes (`par`), `define-sort`,
sort constructors of any arity, and `(as f τ)`.

**Decision procedure: as the monomorphic theories.** Polymorphism is compiled away by the parser, before
any theory sees a term: every use of a polymorphic symbol is a use of a monomorphic instance, and every
instance of a parametric datatype is a datatype of its own.

## What you can write

A sort parameter makes a rank polymorphic; the parameter is bound by the arguments' sorts at each use, and
the instances are unrelated functions:

```smt2
(declare-sort-parameter X)
(declare-fun f (X) X)
(declare-const x Int)
(assert (= (f x) 5))
(assert (= (f true) false))
(assert (= (f (f x)) 7))
(check-sat)
```

A parametric datatype is instantiated sort by sort — `(List Int)` and `(List Bool)` are two datatypes — and
a constructor whose sort the arguments do not determine, such as `nil`, is named with `as`:

```smt2
(declare-datatype List (par (T) ((nil) (cons (head T) (tail (List T))))))
(declare-const l (List Int))
(declare-const m (List Bool))
(assert (= l (cons 1 (cons 2 (as nil (List Int))))))
(assert (= m (cons true (as nil (List Bool)))))
(assert (= (head (tail l)) 2))
(assert (match m ((nil false) ((cons h _) h))))
(check-sat)
```

`define-sort` abbreviates a sort, with parameters of its own; `declare-sort` with an arity declares a sort
constructor, each instance of which is an uninterpreted sort; and a `define-fun` whose rank has a
parameter is read again at each instance:

```smt2
(declare-sort-parameter X)
(declare-sort Box 1)
(define-sort Pairs (A) (Box (Box A)))
(declare-fun unbox ((Box X)) X)
(define-fun same ((a X) (b X)) Bool (= a b))
(declare-const s (Box Int))
(declare-const w (Pairs Bool))
(assert (= (unbox s) 1))
(assert (same (unbox (unbox w)) true))
(assert (not (same (unbox s) 1)))
(check-sat)
```

## What it means, and what it does not

An instance of `f` at `Int` is the monomorphic symbol `f@Int`, declared the first time it is used; an
instance of `List` at `Int` is the datatype `(List Int)` with constructors `nil@Int` and `cons@Int`. That is
what models and `get-value` print, and what the [lowered script](../components/vik.md) contains: the
solver never sees a sort parameter.

A use must determine its instance: `(f x)` binds `X` to the sort of `x`, `(as nil (List Int))` names it,
and a use that does neither — a constant of sort `X` on its own — is refused with a message asking for
`as`. The standard reads an assertion that still mentions a sort parameter as one assertion per
monomorphic sort in the context; VIK does not, since the instances of a quantifier-free assertion would
have nothing to say about one another, and there may be infinitely many.
