---
title: Algebraic datatypes
extraJS:
  - js/snippets.js
extraCSS:
  - css/snippets.css
---

# Algebraic datatypes

Monomorphic datatypes: constructors, selectors and testers, with fields of any supported sort.

## What you can write

```smt2
(declare-datatype List ((nil) (cons (head Int) (tail List))))
(declare-const l List)
(assert (= l (cons 1 (cons 2 nil))))
(assert (= (head (tail l)) 3))
(check-sat)
```

Constructors are injective and distinct, and no value contains itself:

```smt2
(declare-datatype List ((nil) (cons (head Int) (tail List))))
(declare-const l List)
(assert (= l (cons 1 l)))
(check-sat)
```

Testers are `((_ is cons) l)`, and every value is built by one of the constructors — which is what makes a
finite datatype finite:

```smt2
(declare-datatype Color ((red) (green) (blue)))
(declare-const a Color)
(declare-const b Color)
(declare-const c Color)
(declare-const d Color)
(assert (distinct a b c d))
(check-sat)
```

Parametric datatypes are refused.

## Internals

The theory is a set of rules of the [e-graph](../components/egraph.md), which calls them when a node is added
and when two classes are merged, and which lets them derive equalities with the premises they hold — so an
equality the rules derive explains itself like any other.

- **Injectivity and distinctness**: a class that gains a second constructor node either merges the arguments
  pairwise (same constructor) or is a conflict (different ones), explained by the two constructor terms.
- **Selectors**: `sel_i(t)` where the class of `t` holds `C(a₁ … aₙ)` is `aᵢ`.
- **Testers**: `is_C(t)` true where the class holds a constructor is decided by comparing the two; where it
  holds none, the true tester expands the term into `C(sel_1(t) … sel_n(t))`.
- **Exhaustiveness**: for every term of a datatype sort, a clause saying that one of its testers holds; for a
  finite sort, the selector terms are expanded first, so that the search is complete on it.
- **Acyclicity**: at the final check, a depth-first search over the constructor nodes of the classes; a cycle
  is a conflict, explained by the constructor equalities along it.

The scheme is the one of Barrett, Shikanian and Tinelli. Fields of sort `Int` or `Bool` work because those
theories exchange equalities with the e-graph, which is the same mechanism as the
[integer combination](integers.md).
