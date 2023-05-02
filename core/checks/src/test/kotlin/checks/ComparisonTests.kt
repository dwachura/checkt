package io.dwsoft.checkt.core.checks

import io.dwsoft.checkt.testing.testsFor
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.string.shouldContain
import io.kotest.property.Arb
import io.kotest.property.Exhaustive
import io.kotest.property.Gen
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.next
import io.kotest.property.exhaustive.of

class ComparisonTests : FreeSpec({
    testsFor<LessThan<Any>, _, _>(
        runFor = comparisonCases(),
        checking = { first },
        validWhen = { it < second },
        check = { LessThan(second) },
        rule = { lessThan(it.second) },
        violationMessage = { it shouldContain "Value must be less than $second" }
    )

    testsFor<LessThanOrEqual<Any>, _, _>(
        runFor = comparisonCases(),
        checking = { first },
        validWhen = { it <= second },
        check = { LessThanOrEqual(second) },
        rule = { notGreaterThan(it.second) },
        violationMessage = { it shouldContain "Value must not be greater than $second" }
    )

    testsFor<GreaterThan<Any>, _, _>(
        runFor = comparisonCases(),
        checking = { first },
        validWhen = { it > second },
        check = { GreaterThan(second) },
        rule = { greaterThan(it.second) },
        violationMessage = { it shouldContain "Value must be greater than $second" }
    )

    testsFor<GreaterThanOrEqual<Any>, _, _>(
        runFor = comparisonCases(),
        checking = { first },
        validWhen = { it >= second },
        check = { GreaterThanOrEqual(second) },
        rule = { notLessThan(it.second) },
        violationMessage = { it shouldContain "Value must not be less than $second" }
    )
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

