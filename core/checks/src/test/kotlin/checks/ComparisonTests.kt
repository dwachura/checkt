package io.dwsoft.checkt.core.checks

import io.dwsoft.checkt.testing.forAll
import io.dwsoft.checkt.testing.shouldNotPass
import io.dwsoft.checkt.testing.shouldPass
import io.kotest.core.spec.style.FreeSpec
import io.kotest.property.Arb
import io.kotest.property.Exhaustive
import io.kotest.property.Gen
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.next
import io.kotest.property.exhaustive.of

class ComparisonTests : FreeSpec({
    "${LessThan::class.simpleName} check" {
        forAll(comparisonCases()) {
            when {
                first < second -> first shouldPass LessThan(second)
                else -> first shouldNotPass LessThan(second)
            }
        }
    }

    "${LessThanOrEqual::class.simpleName} check" {
        forAll(comparisonCases()) {
            when {
                first > second -> first shouldNotPass LessThanOrEqual(second)
                else -> first shouldPass LessThanOrEqual(second)
            }
        }
    }

    "${GreaterThan::class.simpleName} check" {
        forAll(comparisonCases()) {
            when {
                first > second -> first shouldPass GreaterThan(second)
                else -> first shouldNotPass GreaterThan(second)
            }
        }
    }

    "${GreaterThanOrEqual::class.simpleName} check" {
        forAll(comparisonCases()) {
            when {
                first < second -> first shouldNotPass GreaterThanOrEqual(second)
                else -> first shouldPass GreaterThanOrEqual(second)
            }
        }
    }
})

private fun comparisonCases(): Gen<Pair<Comparable<Any>, Any>> {
    val any = Any()
    return Exhaustive.of(
        lessThan(any),
        greaterThan(any),
        equals(any),
    )
}

private fun lessThan(any: Any): Pair<Comparable<Any>, Any> =
    intLessThanHashCodeOf(any).asComparableToAny() to any

private fun intLessThanHashCodeOf(any: Any) = Arb.int().filter { it < any.hashCode() }.next()

private fun greaterThan(any: Any): Pair<Comparable<Any>, Any> =
    intGreaterThanHashCodeOf(any).asComparableToAny() to any

private fun intGreaterThanHashCodeOf(any: Any) = Arb.int().filter { it > any.hashCode() }.next()

private fun equals(any: Any): Pair<Comparable<Any>, Any> =
    any.hashCode().asComparableToAny() to any

private fun Int.asComparableToAny() =
    object : Comparable<Any> {
        override fun compareTo(other: Any): Int =
            this@asComparableToAny.compareTo(other.hashCode())
    }

