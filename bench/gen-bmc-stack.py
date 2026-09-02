#!/usr/bin/env python3
"""The generator of corpus/QF_DT/vik/bmc__stack_k20.smt2: gen-bmc-stack.py [steps=20] > file.smt2

Bounded model checking over a datatype, the way a BMC tool drives an incremental solver: a stack of
integers takes push and pop moves, unrolled step by step; at each depth the step's postcondition is
checked (unsat) and a reachability question asked (sat), each under push/pop. Datatypes and integers
together. The checks are local to their depth, so the script grows linearly with the steps and the
solvers stay in milliseconds per check; a global invariant over all earlier values was exponential.
"""
import sys

K = int(sys.argv[1]) if len(sys.argv) > 1 else 20
out = []
p = out.append
p("; Hand-written for VIK (this repository's license), generated: bounded model checking over a")
p("; datatype, the way a BMC tool drives an incremental solver. A stack of integers takes push and")
p(f"; pop moves, unrolled {K} steps; at each depth the step's postcondition is checked (unsat) and a")
p("; reachability question asked (sat), each under push/pop. Datatypes and integers together.")
p("(set-logic QF_UFDTLIA)")
# The constructor is `cons`, not `push`: SMTInterpol and princess read a command name there as a syntax error.
p("(declare-datatypes ((Stack 0)) (((cons (top Int) (rest Stack)) (empty))))")
p("(declare-const s0 Stack)")
p("(declare-const n0 Int)")
p("(assert (= s0 empty))")
p("(assert (= n0 0))")
for k in range(K):
    a, b = k, k + 1
    p(f"(declare-const s{b} Stack)")
    p(f"(declare-const n{b} Int)")
    p(f"(declare-const v{a} Int)")
    p(f"(declare-const c{a} Bool)")
    # the move: push a value between 0 and 1000, or pop when the stack is not empty
    p(f"(assert (=> c{a} (and (>= v{a} 0) (<= v{a} 1000) (= s{b} (cons v{a} s{a})) (= n{b} (+ n{a} 1)))))")
    p(f"(assert (=> (not c{a}) (and ((_ is cons) s{a}) (= s{b} (rest s{a})) (= n{b} (- n{a} 1)))))")
    # the postcondition of a push: the new top is the value, the rest the old stack, the counter grew
    p("(push 1)")
    p(f"(assert (not (=> c{a} (and (= (top s{b}) v{a}) (= (rest s{b}) s{a}) (> n{b} n{a})))))")
    p("(set-info :status unsat)")
    p("(check-sat)")
    p("(pop 1)")
    # the reachability question: a stack of exactly this depth whose top exceeds the depth
    p("(push 1)")
    p(f"(assert (and (= n{b} {b}) ((_ is cons) s{b}) (> (top s{b}) {b})))")
    p("(set-info :status sat)")
    p("(check-sat)")
    p("(pop 1)")
print("\n".join(out))
