package io.dwsoft.checkt.core.checks

import io.dwsoft.checkt.testing.testsFor
import io.kotest.core.spec.style.FreeSpec
import io.kotest.property.Arb
import io.kotest.property.Exhaustive
import io.kotest.property.Gen
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.filterNot
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.next
import io.kotest.property.exhaustive.of

class RangeTests : FreeSpec({
    testsFor(casesFor(anyRange())) {
        fromCase(take = { element }) {
            check { InRange(case.range) } shouldPassWhen { value in case.range }

            rule { inRange(case.range) } shouldPassWhen { value in case.range } orFail {
                withMessage("Value must be in range ${case.range}")
            }

            check { OutsideRange(case.range) } shouldPassWhen { value !in case.range }

            rule { outsideRange(case.range) } shouldPassWhen { value !in case.range } orFail {
                withMessage("Value must not be in range ${case.range}")
            }
        }
    }
})

private fun casesFor(range: IntRange): Gen<RangeCase<Int>> =
    Exhaustive.of(
        numberIn(range),
        numberOutside(range),
    )

private fun numberOutside(range: IntRange): RangeCase<Int> =
    RangeCase(Arb.int().filterNot { it in range }.next(), range)

private fun numberIn(range: IntRange): RangeCase<Int> =
    RangeCase(Arb.int(range = range).next(), range)

private fun anyRange() =
    arbitrary {
        val n = Arb.int().bind()
        IntRange(n, Arb.int().filter { it > n }.bind())
    }.next()

private data class RangeCase<T : Comparable<T>>(
    val element: T,
    val range: ClosedRange<T>,
)
