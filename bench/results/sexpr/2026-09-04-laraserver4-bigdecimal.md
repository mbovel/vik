# Numbers read by `BigDecimal`, 2026-09-04, laraserver4

The lexer now gives each token its lexical class (SMT-LIB 2.6, 3.1), so it has to tell a numeral from a
decimal from a symbol. It did that with several passes over the token — `forall(_.isDigit)`, `indexOf('.')`,
`count(_ == '.')`, two more `forall`s over the halves and a concatenation before `BigInt` — which reads
poorly ([issue 6](https://github.com/mbovel/vik/issues/6)). A token that opens with a digit is a number,
since a symbol may not, so `java.math.BigDecimal` can read the digits and the point in one pass, and its
scale says which of the two numbers it is:

```scala
else if atom.head.isDigit then
  try
    val number = java.math.BigDecimal(atom)
    if number.scale > 0 then SExpr.SDecimal(BigInt(number.unscaledValue.nn), number.scale)
    else SExpr.SNumeral(BigInt(number.toBigIntegerExact.nn))
  catch case _: NumberFormatException => SExpr.SSymbol(atom)
```

`SExprBench.parse`, JMH `-f 2 -wi 3 -i 3`, ms/op
([before](2026-09-04-laraserver4-bigdecimal-before.json), [after](2026-09-04-laraserver4-bigdecimal-after.json)):

| Params | Before | After | Ratio |
|---|---:|---:|---:|
| n=1000 | 2.282 ± 0.289 | 2.326 ± 0.268 | 0.98× |
| n=10000 | 24.042 ± 2.221 | 23.270 ± 2.228 | 1.03× |

**A wash**, both within the intervals: a quarter of the generated tokens are numerals, and the passes it
saves are over tokens of three or four characters. Kept for the reading, not the speed — six lines against
ten, one pass, and the digits parsed by the library rather than by hand.

What it accepts changes at the edges, on tokens SMT-LIB does not define: `1e5` and `1E+2` were symbols
beginning with a digit, which is no symbol at all, and are now numbers. A token opening with `+` or `-`
stays a symbol, as the standard has it (`-` is a symbol, and so is `-1.2`), because the digit decides
whether `BigDecimal` is asked at all.

The whole corpus, 5443 files, answers as before.
