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
import io.kotest.matchers.string.shouldContain
import io.kotest.property.Exhaustive
import io.kotest.property.Gen
import io.kotest.property.exhaustive.of

class NullabilityTests : FreeSpec({
    testsFor<NotNull, _, _>(
        runFor = nullabilityCases(),
        checking = { value },
        validWhen = { it != null },
        check = { NotNull },
        rule = { notBeNull() },
        violationMessage = { it shouldContain "Value must not be null" }
    )

    testsFor<IsNull, _, _>(
        runFor = nullabilityCases(),
        checking = { value },
        validWhen = { it == null },
        check = { IsNull },
        rule = { beNull() },
        violationMessage = { it shouldContain "Value must be null" }
    )

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

