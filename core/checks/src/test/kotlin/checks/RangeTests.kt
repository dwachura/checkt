package io.dwsoft.checkt.core.checks

import io.dwsoft.checkt.core.validation
import io.dwsoft.checkt.testing.forAll
import io.dwsoft.checkt.testing.shouldBeInvalidBecause
import io.dwsoft.checkt.testing.shouldBeValid
import io.dwsoft.checkt.testing.shouldNotPass
import io.dwsoft.checkt.testing.shouldPass
import io.dwsoft.checkt.testing.testValidation
import io.dwsoft.checkt.testing.violated
import io.kotest.core.spec.style.FreeSpec
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.string.shouldContain
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
    "${InRange::class.simpleName}" - {
        forAll(casesFor(anyRange())) {
            "Check works" {
                when (element) {
                    in range -> element shouldPass InRange(range)
                    else -> element shouldNotPass InRange(range)
                }
            }

            "Rule works" {
                testValidation(
                    of = element,
                    with = validation { +inRange(range) }
                ) {
                    when (element) {
                        in range -> result.shouldBeValid()
                        else -> result.shouldBeInvalidBecause(
                            validated.violated<InRange<*>> { msg ->
                                msg shouldContain "Value must be in range $range"
                            }
                        )
                    }
                }
            }
        }
    }

    "${OutsideRange::class.simpleName}" - {
        forAll(casesFor(anyRange())) {
            "Check works" {
                when (element) {
                    in range -> element shouldNotPass OutsideRange(range)
                    else -> element shouldPass OutsideRange(range)
                }
            }

            "Rule works" {
                testValidation(
                    of = element,
                    with = validation { +outsideRange(range) }
                ) {
                    when (element) {
                        in range -> result.shouldBeInvalidBecause(
                            validated.violated<OutsideRange<*>> { msg ->
                                msg shouldContain "Value must not be in range $range"
                            }
                        )
                        else -> result.shouldBeValid()
                    }
                }
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
