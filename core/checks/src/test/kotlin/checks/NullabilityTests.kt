package io.dwsoft.checkt.core.checks

import io.dwsoft.checkt.core.validation
import io.dwsoft.checkt.testing.failWithMessage
import io.dwsoft.checkt.testing.failed
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

class NullabilityTests : FreeSpec({
    "${NotNull::class.simpleName}" - {
        forAll(nullabilityCases()) {
            "Check works" {
                when {
                    value != null -> value shouldPass NotNull
                    else -> value shouldNotPass NotNull
                }
            }

            "Rule works" {
                testValidation(
                    of = value,
                    with = validation { +notBeNull() }
                ) {
                    when {
                        value != null -> result.shouldBeValid()
                        else -> result.shouldBeInvalidBecause(
                            validated.violated<NotNull> { msg ->
                                msg shouldContain "Value must not be null"
                            }
                        )
                    }
                }
            }
        }
    }

    "${IsNull::class.simpleName}" - {
        forAll(nullabilityCases()) {
            "Check works" {
                when {
                    value != null -> value shouldNotPass IsNull
                    else -> value shouldPass IsNull
                }
            }

            "Rule works" {
                testValidation(
                    of = value,
                    with = validation { +beNull() }
                ) {
                    when (value) {
                        null -> result.shouldBeValid()
                        else -> result.shouldBeInvalidBecause(
                            validated.violated<IsNull> { msg ->
                                msg shouldContain "Value must be null"
                            }
                        )
                    }
                }
            }
        }
    }

    "Rules are applied only when subject is non-null" {
        forAll(nullabilityCases()) {
            testValidation(
                of = value,
                with = validation {
                    notNullAnd(notNullErrorMessage = { "1" }) {
                        +failWithMessage { "2" }
                    }
                }
            ) {
                when (value) {
                    null -> result.shouldBeInvalidBecause(
                        validated.violated<NotNull>(withMessage = "1")
                    )

                    else -> result.shouldBeInvalidBecause(
                        validated.failed(withMessage = "2")
                    )
                }
            }
        }
    }
})

private fun nullabilityCases(): Gen<NullabilityCase> =
    Exhaustive.of(
        notNullValue(),
        nullValue()
    )

private fun notNullValue(): NullabilityCase =
    NullabilityCase(Any())

private fun nullValue(): NullabilityCase =
    NullabilityCase(null)

private data class NullabilityCase(val value: Any?)

