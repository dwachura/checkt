package io.dwsoft.checkt.core.checks

import io.dwsoft.checkt.core.validation
import io.dwsoft.checkt.testing.failWithMessage
import io.dwsoft.checkt.testing.failed
import io.dwsoft.checkt.testing.forAll
import io.dwsoft.checkt.testing.shouldBeInvalidBecause
import io.dwsoft.checkt.testing.shouldNotPass
import io.dwsoft.checkt.testing.shouldPass
import io.dwsoft.checkt.testing.testValidation
import io.dwsoft.checkt.testing.violated
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.property.Exhaustive
import io.kotest.property.Gen
import io.kotest.property.exhaustive.of

class NullabilityTests : StringSpec({
    "${NonNull::class.simpleName} check" {
        forAll(nullabilityCases()) {
            when {
                value != null -> value shouldPass NonNull()
                else -> value shouldNotPass NonNull()
            }
        }
    }

    "${IsNull::class.simpleName} check" {
        forAll(nullabilityCases()) {
            when {
                value != null -> value shouldNotPass IsNull()
                else -> value shouldPass IsNull()
            }
        }
    }

    // TODO: refactor
    "test1" {
        val obj = Any()
        testValidation(
            of = (obj as Any?),
            with = validation {
                subjectNotNullAnd { +failWithMessage { "1" } }
            },
        ) {
            result.shouldBeInvalidBecause(
                obj.failed(withMessage = "1")
            )
        }
    }

    "test2" {
        val obj: Any? = null
        testValidation(
            of = obj,
            with = validation {
                subjectNotNullAnd { +failWithMessage { "1" } }
            },
        ) {
            result.shouldBeInvalidBecause(
                obj.violated<NonNull<*>>()
            ).violations shouldHaveSize 1
        }
    }
})

private fun nullabilityCases(): Gen<NullabilityCase> =
    Exhaustive.of(
        nonNullValue(),
        nullValue()
    )

private fun nonNullValue(): NullabilityCase =
    NullabilityCase(Any())

private fun nullValue(): NullabilityCase =
    NullabilityCase(null)

private data class NullabilityCase(val value: Any?)

