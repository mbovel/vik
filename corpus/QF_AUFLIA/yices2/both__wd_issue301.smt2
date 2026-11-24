; From yices2: both/wd_issue301.smt2 (BSD-3-Clause, LICENSES/yices2.txt)
; logic: QF_AUFLIA, the smallest the script fits, for QF_ALIRA (prune logic)
(set-logic QF_AUFLIA)
(declare-fun _substvar_2673_ () Bool)
(declare-fun _substvar_4177_ () Bool)

(declare-fun _substvar_4583_ () (Array (Array Bool (Array (Array (Array Int (Array Real Real)) (Array Real (Array Real Real))) (Array (Array (Array (Array (Array (Array Real Real) Real) Real) Bool) (Array (Array (Array (Array (Array Real Real) Int) (Array (Array (Array (Array Real Real) Int) (Array (Array (Array (Array Real (Array (Array Real Real) Real)) (Array Real Real)) (Array Int (Array Real (Array (Array Real Real) Real)))) (Array (Array Real Real) Int))) Bool)) (Array (Array (Array (Array Real Real) Real) Real) Bool)) (Array (Array Real Real) (Array (Array (Array Real (Array (Array Real Real) Real)) (Array Real (Array (Array Real Real) Real))) (Array Int (Array Real (Array (Array Real Real) Real))))))) (Array (Array (Array Real (Array (Array Real Real) Real)) (Array Real (Array Real Real))) (Array (Array (Array (Array Real (Array (Array Real Real) Real)) (Array Real Real)) (Array Int (Array Real (Array (Array Real Real) Real)))) (Array (Array Real Real) Int)))))) (Array (Array (Array (Array Real Real) Int) (Array (Array (Array (Array Real (Array (Array Real Real) Real)) (Array Real Real)) (Array Int (Array Real (Array (Array Real Real) Real)))) (Array (Array Real Real) Int))) Bool)))

(declare-const aaa (Array (Array Bool (Array (Array (Array Int (Array Real Real)) (Array Real (Array Real Real))) (Array (Array (Array (Array (Array (Array Real Real) Real) Real) Bool) (Array (Array (Array (Array (Array Real Real) Int) (Array (Array (Array (Array Real Real) Int) (Array (Array (Array (Array Real (Array (Array Real Real) Real)) (Array Real Real)) (Array Int (Array Real (Array (Array Real Real) Real)))) (Array (Array Real Real) Int))) Bool)) (Array (Array (Array (Array Real Real) Real) Real) Bool)) (Array (Array Real Real) (Array (Array (Array Real (Array (Array Real Real) Real)) (Array Real (Array (Array Real Real) Real))) (Array Int (Array Real (Array (Array Real Real) Real))))))) (Array (Array (Array Real (Array (Array Real Real) Real)) (Array Real (Array Real Real))) (Array (Array (Array (Array Real (Array (Array Real Real) Real)) (Array Real Real)) (Array Int (Array Real (Array (Array Real Real) Real)))) (Array (Array Real Real) Int)))))) (Array (Array (Array (Array Real Real) Int) (Array (Array (Array (Array Real (Array (Array Real Real) Real)) (Array Real Real)) (Array Int (Array Real (Array (Array Real Real) Real)))) (Array (Array Real Real) Int))) Bool)))

(assert (and _substvar_2673_ (or _substvar_4177_ (= aaa _substvar_4583_ aaa aaa))))

(set-info :status sat)
(check-sat)


