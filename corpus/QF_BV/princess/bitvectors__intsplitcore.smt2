; From princess: bitvectors/intsplitcore.smt2 (BSD, LICENSES/princess.txt)
; logic: QF_BV, the smallest the script fits, which set none (prune logic)
(set-logic QF_BV)
(set-info :smt-lib-version 2.6)
(declare-fun x () (_ BitVec 2))
(declare-fun z () (_ BitVec 8))

(assert
   (and
	; (= z #b00001111)
	(= ((_ extract 7 4) z) #b0000)
	(= ((_ extract 3 0) z) #b1111)	
	; (not (= ((_ extract 7 4) z) ((_ extract 3 0) z)))
	(= x ((_ extract 1 0) z))
	(or
	  (= x ((_ extract 7 6) z))
	  (= x ((_ extract 6 5) z))	  
	  ; (= x ((_ extract 1 0) z))	  
	)
   )	
)	
(set-info :status unsat)
(check-sat)
