package io.dwsoft.checkt.core.checks

import io.dwsoft.checkt.core.validation
import io.dwsoft.checkt.testing.failWithMessage
import io.dwsoft.checkt.testing.failed
import io.dwsoft.checkt.testing.forAll
import io.dwsoft.checkt.testing.shouldBeInvalidBecause
import io.dwsoft.checkt.testing.testValidation
import io.dwsoft.checkt.testing.testsFor
import io.dwsoft.checkt.testing.violated
import io.kotest.core.spec.style.FreeSpec
import io.kotest.property.Exhaustive
import io.kotest.property.Gen
import io.kotest.property.exhaustive.of

class NullabilityTests : FreeSpec({
    testsFor(nullabilityCases()) {
        takingCaseAsValue {
            check { NotNull } shouldPassWhen { value != null }

            rule { notBeNull() } shouldPassWhen { value != null } orFail {
                withMessage("Value must not be null")
            }

            check { IsNull } shouldPassWhen { value == null }

            rule { beNull() } shouldPassWhen { value == null } orFail {
                withMessage("Value must be null")
            }
        }
    }

    "Rules are applied only when subject is non-null" {
        forAll(nullabilityCases()) {
            val value = this
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
                        validated.violated(NotNull.RuleDescriptor) { withMessage("1") }
                    )
                    else -> result.shouldBeInvalidBecause(
                        validated.failed { withMessage("2") }
                    )
                }
            }
        }
    }
})

private fun nullabilityCases(): Gen<Any?> = Exhaustive.of(Any(), null)

