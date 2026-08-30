---
title: A tour of the solver
extraJS:
  - js/snippets.js
extraCSS:
  - css/snippets.css
---

# A tour of the solver

Every script below runs in your browser: press the play button in the corner of a block. They are editable,
so change one and run it again.

## Uninterpreted functions

Congruence: equal arguments give equal results, and that is all the solver assumes about `f`.

```smt2
(declare-sort U 0)
(declare-fun f (U U) U)
(declare-const a U)
(declare-const b U)
(assert (= a b))
(assert (not (= (f a b) (f b a))))
(check-sat)
```

## Linear integer arithmetic

Bounds on linear combinations, decided by a simplex over the integers with branch and bound.

```smt2
(declare-const x Int)
(declare-const y Int)
(assert (<= (+ (* 2 x) (* 3 y)) 10))
(assert (>= (+ x y) 4))
(assert (>= x 0))
(assert (>= y 0))
(check-sat)
```

Integrality matters: the same system over the rationals has a solution, over the integers it does not.

```smt2
(declare-const x Int)
(assert (< (* 2 x) 3))
(assert (> (* 2 x) 1))
(check-sat)
```

## Functions over integers

The two theories exchange what they know about the terms they share: here `x = y` follows from arithmetic, and
congruence then equates the two applications.

```smt2
(declare-fun f (Int) Int)
(declare-const x Int)
(declare-const y Int)
(assert (<= x y))
(assert (<= y x))
(assert (not (= (f x) (f y))))
(check-sat)
```

## Algebraic datatypes

Constructors are injective and distinct, selectors and testers follow them, and no value contains itself.

```smt2
(declare-datatype List ((nil) (cons (head Int) (tail List))))
(declare-const l List)
(assert (= l (cons 1 (cons 2 nil))))
(assert (= (head (tail l)) 3))
(check-sat)
```

```smt2
(declare-datatype List ((nil) (cons (head Int) (tail List))))
(declare-const l List)
(assert (= l (cons 1 l)))
(check-sat)
```

## Lambdas

Functions are values: `(-> Int Int)` is a sort, `lambda` makes a function, and applying one reduces it.

```smt2
(declare-fun f (Int) Int)
(declare-const g (-> Int Int))
(assert (= g (lambda ((x Int)) (+ (f x) 1))))
(assert (not (= (g 2) (+ (f 2) 1))))
(check-sat)
```

Two lambdas with the same body are the same function, whatever their parameters are called, and a function
that differs from another differs on some argument (extensionality).

```smt2
(declare-fun f (Int) Int)
(assert (not (= (lambda ((x Int)) (f x)) (lambda ((y Int)) (f y)))))
(check-sat)
```

## Bit-vectors, without bit-blasting

The arithmetic fragment of `QF_BV` is lowered to integers: additions modulo, comparisons, masks, `extract`,
`concat`, extensions. Wide vectors cost no more than narrow ones.

```smt2
(set-logic QF_BV)
(declare-const a (_ BitVec 64))
(declare-const b (_ BitVec 64))
(assert (bvult a b))
(assert (bvult b a))
(check-sat)
```

## Incremental use

`push` and `pop` keep the work done so far; each `check-sat` answers the assertions of the levels below it.

```smt2
(declare-const x Int)
(assert (> x 0))
(check-sat)
(push 1)
(assert (< x 0))
(check-sat)
(pop 1)
(check-sat)
```

## Budgets

A `check-sat` answers `unknown` when it runs out of fuel — a number of solver steps, so the answer is the same
on every machine — and `(get-info :reason-unknown)` says why.

```smt2
(set-option :rlimit 1)
(declare-fun f (Int) Int)
(declare-const x Int)
(declare-const y Int)
(assert (or (= (f x) 1) (= (f y) 2)))
(assert (< x y))
(check-sat)
(get-info :reason-unknown)
```
