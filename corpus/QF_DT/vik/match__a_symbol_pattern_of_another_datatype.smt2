; Hand-written for VIK (this repository's license): a `match` whose symbol pattern is a constructor of
; another datatype. A symbol pattern is a constructor pattern only for a constructor of the scrutinee's
; datatype, and any other symbol is a variable binding the whole scrutinee; VIK read `red` as the
; constructor, bound nothing, and answered sat where the match is `(f l)` (issue 23, 2026-09-05).
(set-logic ALL)
(declare-datatype Color ((red) (green)))
(declare-datatype Lamp ((lit) (dark)))
(declare-fun f (Lamp) Int)
(declare-const l Lamp)
(assert (= (f lit) 1))
(assert (= (f dark) 2))
(assert ((_ is lit) l))
(assert (= 2 (match l ((red (f red))))))
(set-info :status unsat)
(check-sat)
