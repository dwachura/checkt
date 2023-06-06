package io.dwsoft.checkt.core.checks

import io.dwsoft.checkt.testing.testsFor
import io.kotest.core.spec.style.FreeSpec
import io.kotest.property.Arb
import io.kotest.property.Exhaustive
import io.kotest.property.Gen
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.next
import io.kotest.property.exhaustive.of

class ComparisonTests : FreeSpec({
    testsFor(comparisonCases()) {
        taking { first } asValue {
            check { LessThan(case.second) } shouldPassWhen { value < case.second }

            rule { lessThan(case.second) } shouldPassWhen { value < case.second } orFail
                    { withMessage("Value must be less than ${case.second}") }

            check { LessThanOrEqual(case.second) } shouldPassWhen { value <= case.second }

            rule { notGreaterThan(case.second) } shouldPassWhen { value <= case.second } orFail
                    { withMessage("Value must not be greater than ${case.second}") }

            check { GreaterThan(case.second) } shouldPassWhen { value > case.second }

            rule { greaterThan(case.second) } shouldPassWhen { value > case.second } orFail
                    { withMessage("Value must be greater than ${case.second}") }

            check { GreaterThanOrEqual(case.second) } shouldPassWhen { value >= case.second }

            rule { notLessThan(case.second) } shouldPassWhen { value >= case.second } orFail
                    { withMessage("Value must not be less than ${case.second}") }
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

