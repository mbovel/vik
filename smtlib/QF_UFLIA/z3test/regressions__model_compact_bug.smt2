; From z3test: model_compact_bug.smt2 (MIT, LICENSES/z3test.txt)

; Copyright (c) 2015 Microsoft Corporation
(set-option :model.compact true)
(declare-fun p (Int) Bool)
(assert (p 1))
(assert (p 2))
(assert (p 3))
(set-info :status sat)
(check-sat-using smt)
(get-model)