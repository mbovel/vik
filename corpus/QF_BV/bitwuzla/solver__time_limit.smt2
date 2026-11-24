; From bitwuzla: solver/time_limit.smt2 (MIT, LICENSES/bitwuzla.txt)
; Exercises the global --time-limit (-t) CLI option, handled by the detached
; timeout thread in src/main/time_limit.cpp. The bvmul-distinct problem below is
; hard enough to exceed the limit, so the timeout thread must fire and print
; "unknown". Regression for the missing predicate in the condition-variable
; wait: a spurious wakeup must not silently cancel the limit.
; statuses: the `unknown` ones answered by This is cvc5 version 1.3.3 [git 8ff882e on branch HEAD] (statusstamp, 2026-11-24)
(set-logic QF_BV)
(set-option :rewrite-level 0)
(set-option :preprocess false)
(declare-const a (_ BitVec 16))
(declare-const b (_ BitVec 16))
(declare-const c (_ BitVec 16))
(assert (distinct (bvmul a b c) (bvmul b c a)))
(set-info :status unsat)
(check-sat)
