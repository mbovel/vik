---
title: Introduction
extraJS:
  - js/snippets.js
extraCSS:
  - css/site.css
  - css/snippets.css
---

# Introduction

An SMT solver in Scala 3, small enough to read: equality with uninterpreted functions, linear integer
arithmetic over integers and reals, algebraic datatypes, arrays, and the arithmetic fragment of
bit-vectors, in about 5000 lines of
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
- [Evaluation](evaluation.md): what it costs, warm and cold, against z3, cvc5, princess and SMTInterpol.
- The [API documentation](api/index.html) of the six modules, and the
  [repository](https://github.com/mbovel/vik).

## The theories

Each page says what you can write, how it is solved, and first of all whether VIK decides it.

| Theory | Decision procedure |
|---|---|
| [First-order logic](theories/first-order-logic.md) | Yes. |
| [Uninterpreted functions](theories/uninterpreted-functions.md) | Yes. |
| [Integer arithmetic](theories/integers.md) | Yes: branch and bound inside the small-model box. |
| [Real arithmetic](theories/reals.md) | Yes. |
| [Algebraic datatypes](theories/datatypes.md) | Yes. |
| [Bit-vectors](theories/bit-vectors.md) | Yes, for the fragment it accepts. |
| [Arrays](theories/arrays.md) | Yes. |
| [Sort parameters](theories/polymorphism.md) | As the theory of each instance: polymorphism is compiled away. |

## The components

Each component is a module usable on its own. Lines are `wc -l` over its `src/main/scala`.

| Component | Lines | What it is |
|---|---|---|
| [The e-graph](components/egraph.md) | 743 | Congruence closure with backtracking, explanations, disequalities and rules. |
| [The CDCL(T) solver](components/cdclt.md) | 569 | A CDCL SAT solver with a theory interface. |
| [The LIA solver](components/lia.md) | 849 | A simplex over the integers with branch and bound. |
| [The S-expression reader](components/sexpr.md) | 85 | S-expressions from a string, one pass. |
| [The language](components/smtlib.md) | 904 | SMT-LIB 2 as written: sorts, terms, formulas, in and out. |
| [The solver](components/vik.md) | 2867 | The solver's trees and their lowering, the simplifier, the encoder, the theories, the solver and the session. |

## What it is for

A verification tool asks many small questions, and pays for each of them: printing a script, starting a
process, parsing the answer. VIK is written to be called instead — `solve()` on a solver you keep, with
`push` and `pop` around the parts that change. Being small matters twice: it is possible to read the whole
thing before trusting it, and every part of it is a library of its own, so a tool that only needs congruence
closure or a simplex can take that and nothing else.

The trade is stated plainly on the [evaluation](evaluation.md) page: on the instances a verifier produces,
VIK is in the same range as the established solvers; on the hard instances of the competition it is not, and
it is not meant to be.
