package io.dwsoft.checkt.core.checks

import io.dwsoft.checkt.core.checks.InRange
import io.dwsoft.checkt.core.checks.OutsideRange
import io.dwsoft.checkt.testing.forAll
import io.dwsoft.checkt.testing.shouldNotPass
import io.dwsoft.checkt.testing.shouldPass
import io.kotest.core.spec.style.StringSpec
import io.kotest.property.Arb
import io.kotest.property.Exhaustive
import io.kotest.property.Gen
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.filterNot
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.next
import io.kotest.property.exhaustive.of

class RangeTests : StringSpec({
    "${InRange::class.simpleName} check" {
        forAll(casesFor(anyRange())) {
            when {
                element in range -> element shouldPass InRange(range)
                else -> element shouldNotPass InRange(range)
            }
        }
    }

    "${OutsideRange::class.simpleName} check" {
        forAll(casesFor(anyRange())) {
            when {
                element in range -> element shouldNotPass OutsideRange(range)
                else -> element shouldPass OutsideRange(range)
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
