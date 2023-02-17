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
import io.kotest.matchers.string.shouldContain
import io.kotest.property.Exhaustive
import io.kotest.property.Gen
import io.kotest.property.exhaustive.of

class EqualityTests : FreeSpec({
    "${Equals::class.simpleName}" - {
        forAll(equalityCases()) {
            "Check works" {
                when {
                    first != second -> first shouldNotPass Equals(second)
                    else -> first shouldPass Equals(second)
                }
            }

            "Rule works" {
                testValidation(
                    of = first,
                    with = validation { +equalTo(second) }
                ) {
                    when {
                        first != second -> result.shouldBeInvalidBecause(
                            validated.violated<Equals<*>> { msg ->
                                msg shouldContain "Value must equal to $second"
                            }
                        )
                        else -> result.shouldBeValid()
                    }
                }
            }
        }
    }

    "${IsDifferent::class.simpleName}" - {
        forAll(equalityCases()) {
            "Check works" {
                when {
                    first != second -> first shouldPass IsDifferent(second)
                    else -> first shouldNotPass IsDifferent(second)
                }
            }

            "Rule works" {
                testValidation(
                    of = first,
                    with = validation { +differentThan(second) }
                ) {
                    when {
                        first != second -> result.shouldBeValid()
                        else -> result.shouldBeInvalidBecause(
                            validated.violated<IsDifferent<*>> { msg ->
                                msg shouldContain "Value must be different than $second"
                            }
                        )
                    }
                }
            }
        }
    }
})

private fun equalityCases(): Gen<Pair<Any?, Any?>> {
    val any = Any()
    return Exhaustive.of(
        Pair(any, any), // equal values
        Pair(any, Any()), // different values
        Pair(any, null), // right value null
        Pair(null, any), // left value null
        Pair(null, null), // both null
    )
}
