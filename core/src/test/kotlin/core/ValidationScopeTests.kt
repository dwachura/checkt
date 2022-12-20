package io.dwsoft.checkt.core

import io.dwsoft.checkt.core.ValidationPath.Segment.Key
import io.dwsoft.checkt.core.ValidationPath.Segment.Name
import io.dwsoft.checkt.core.ValidationPath.Segment.NumericIndex
import io.dwsoft.checkt.core.ValidationStatus.Valid
import io.dwsoft.checkt.testing.ValidationPathBuilder
import io.dwsoft.checkt.testing.failWithMessage
import io.dwsoft.checkt.testing.failed
import io.dwsoft.checkt.testing.forAll
import io.dwsoft.checkt.testing.notBlankString
import io.dwsoft.checkt.testing.pass
import io.dwsoft.checkt.testing.shouldBeInvalidBecause
import io.kotest.assertions.fail
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.Exhaustive
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.positiveInt
import io.kotest.property.exhaustive.of

class ValidationScopeTests : FreeSpec({
    val failingRule = ValidationRule.failWithMessage { "$value" }

    "Status of successfully validated scope is ${Valid::class.simpleName}" {
        val validationScope = ValidationScope(ValidationPath(!"seg1"))

        with(validationScope) {
            validate(Any(), ValidationRule.pass)
        }

        validationScope.status shouldBe Valid
    }

    "Single rule validation..." - {
        "...returns validation status" {
            val scope = ValidationScope()

            val status = scope.validate("v1", failingRule).status

            status.shouldBeInvalidBecause("v1".failed(withMessage = "v1"))
        }

        "...statuses are merged into the scope's status" {
            val status = ValidationScope().apply {
                validate("v1", failingRule)
                validate("v2", failingRule)
                validate("v3", failingRule)
            }.status

            status.shouldBeInvalidBecause(
                "v1".failed(withMessage = "v1"),
                "v2".failed(withMessage = "v2"),
                "v3".failed(withMessage = "v3"),
            )
        }
    }

    "Block validation..." - {
        "...returns status of all internal operations" {
            val status = ValidationScope().validate {
                validate("v1", failingRule)
                validate("v2", failingRule)
            }.status

            status.shouldBeInvalidBecause(
                "v1".failed(withMessage = "v1"),
                "v2".failed(withMessage = "v2"),
            )
        }

        "...statuses are merged into the scope's status" {
            val status = ValidationScope().apply {
                validate { validate("v1", failingRule) }
                validate { validate("v2", failingRule) }
                validate { validate("v3", failingRule) }
            }.status

            status.shouldBeInvalidBecause(
                "v1".failed(withMessage = "v1"),
                "v2".failed(withMessage = "v2"),
                "v3".failed(withMessage = "v3"),
            )
        }

        "...can be nested under different paths" {
            suspend fun ValidationScope.failOn(value: Any) = validate(value, failingRule)

            forAll(nestingCases()) {
                val expectedViolation =
                    "v".failed(withMessage = "v", underPath = { root + this@forAll })
                val scope = ValidationScope()

                val nestedStatus = when (this) {
                    is Name -> scope.validate(rawValue) { failOn("v") }
                    is NumericIndex -> scope.validate(this) { failOn("v") }
                    is Key -> scope.validate(this) { failOn("v") }
                    else -> fail("Unexpected segment")
                }.status

                nestedStatus.shouldBeInvalidBecause(expectedViolation)
                scope.status.shouldBeInvalidBecause(expectedViolation)
            }
        }
    }
})

private fun nestingCases(): Exhaustive<ValidationPath.Segment> =
    Exhaustive.of(
        Name(Arb.notBlankString(maxSize = 10).next()),
        Arb.positiveInt(max = 10).next().asIndex(),
        Arb.notBlankString(maxSize = 10).next().asKey(),
    )
