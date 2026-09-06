# The bench drivers, the corpus tools and the platforms, 2026-09-06, laraserver4

The last eight findings of the second audit ([issues 53 to 60](https://github.com/mbovel/vik/issues/53)).
The bench: z3's driver divided by the first divisor only for `(/ a b c)` (princess's too), the replay
compiler expanded a macro applied to the wrong number of arguments, and carried one logic for a script
that sets another after `reset`. The corpus tools: the importer stamped a status into a commented-out
`(check-sat)`, and the status stamper never reported a disagreement since z3 and cvc5 error on a status
they contradict. The platforms: the terms' hash went through the platform's `BigInteger.hashCode` past
2^63 — the JDK's and the javalib's differ — so the model rows and the `bit!` numbering of a 64-bit
script differed between the JVM and Scala.js; the lexer's `\s` had the vertical tab on the JVM and not
on Scala Native; the command line wrote a non-ASCII symbol as `?` under a C locale.

The hash was first replaced by one of its own, and two 64-bit files of the corpus fell over the test's
budget: the JVM's iteration order changed, and the search with it. So the JDK's algorithm is computed
on every platform instead — on the JVM the very value `BigInt.hashCode` gives, checked on 207 numbers —
and nothing moves there. The Scala.js bundle now answers the audit's two scripts as the JVM does.

Cold, VIK on the JVM, the 44 evaluation instances, the median of three runs: 29.06 s before, 29.16 s
after, no instance past the noise.

**Kept.**
