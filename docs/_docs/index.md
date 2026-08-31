---
title: Introduction
extraJS:
  - js/snippets.js
extraCSS:
  - css/site.css
  - css/snippets.css
---

# VIK

An SMT solver in Scala 3, small enough to read: equality with uninterpreted functions, linear integer
arithmetic, algebraic datatypes, lambdas, and the arithmetic fragment of bit-vectors, under 5000 lines of
solver. It runs on the JVM, in the browser and as a native binary, and it is meant to be embedded — as a
library, in the process of the tool that asks the questions.

The snippets of this site are solved here, in your browser: press the **play button** in the corner of a block. They are
editable, so change one and run it again.

```smt2
(declare-fun f (Int) Int)
(declare-const x Int)
(assert (= (f x) 1))
(assert (not (= (f (+ x 0)) 1)))
(check-sat)
```

## Where to go

- [Getting started](getting-started.md): the command line, and the Scala API.
- The theories, each with what you can write and how it is decided:
  [first-order logic](theories/first-order-logic.md), [uninterpreted functions](theories/uninterpreted-functions.md),
  [integer arithmetic](theories/integers.md), [algebraic datatypes](theories/datatypes.md),
  [bit-vectors](theories/bit-vectors.md), [functions as values](theories/lambdas.md).
- The components, usable on their own: [the e-graph](components/egraph.md), [the CDCL(T) solver](components/cdclt.md),
  [the LIA solver](components/lia.md), [the front end](components/front-end.md).
- [Evaluation](evaluation.md): what it costs, warm and cold, against z3, cvc5, princess and SMTInterpol.
- The [API documentation](api/index.html) of the six modules, and the
  [repository](https://github.com/mbovel/vik).

## What it is for

A verification tool asks many small questions, and pays for each of them: printing a script, starting a
process, parsing the answer. VIK is written to be called instead — `solve()` on a solver you keep, with
`push` and `pop` around the parts that change. Being small matters twice: it is possible to read the whole
thing before trusting it, and every part of it is a library of its own, so a tool that only needs congruence
closure or a simplex can take that and nothing else.

The trade is stated plainly on the [evaluation](evaluation.md) page: on the instances a verifier produces,
VIK is in the same range as the established solvers; on the hard instances of the competition it is not, and
it is not meant to be.
