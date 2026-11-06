; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/invalid/RedBlackTree.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_UFBVDT)
(declare-fun start!18 () Bool)
(assert start!18)
(declare-datatypes ((Color!6 0))(
  ( (Red!6) (Black!6) )
))
(declare-fun c!30 () Color!6)
(declare-datatypes ((Tree!6 0))(
  ( (Empty!6) (Node!6 (color!55 Color!6) (left!57 Tree!6) (value!90 (_ BitVec 32)) (right!57 Tree!6)) )
))
(declare-fun a!44 () Tree!6)
(declare-fun b!35 () Tree!6)
(get-info :version)
(assert (=> start!18 (and (or (not ((_ is Black!6) c!30)) (not ((_ is Node!6) a!44)) (not ((_ is Red!6) (color!55 a!44))) (not ((_ is Node!6) (left!57 a!44))) (not ((_ is Red!6) (color!55 (left!57 a!44))))) (or (not ((_ is Black!6) c!30)) (not ((_ is Node!6) a!44)) (not ((_ is Red!6) (color!55 a!44))) (not ((_ is Node!6) (right!57 a!44))) (not ((_ is Red!6) (color!55 (right!57 a!44))))) (or (not ((_ is Black!6) c!30)) (not ((_ is Node!6) b!35)) (not ((_ is Red!6) (color!55 b!35))) (not ((_ is Node!6) (left!57 b!35))) (not ((_ is Red!6) (color!55 (left!57 b!35))))) (or (not ((_ is Black!6) c!30)) (not ((_ is Node!6) b!35)) (not ((_ is Red!6) (color!55 b!35))) (not ((_ is Node!6) (right!57 b!35))) (not ((_ is Red!6) (color!55 (right!57 b!35))))))))
(assert (=> start!18 true))
(set-info :status sat)
(check-sat)
(get-model)
