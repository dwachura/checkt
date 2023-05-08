package io.dwsoft.checkt.core

import io.dwsoft.checkt.core.ValidationPath.Element.Index
import io.dwsoft.checkt.core.ValidationPath.Element.Segment
import io.dwsoft.checkt.core.ValidationScope.NamingUniquenessException
import io.dwsoft.checkt.core.ValidationStatus.Valid
import io.dwsoft.checkt.testing.failWithMessage
import io.dwsoft.checkt.testing.failed
import io.dwsoft.checkt.testing.forAll
import io.dwsoft.checkt.testing.notBlankString
import io.dwsoft.checkt.testing.pass
import io.dwsoft.checkt.testing.shouldBeInvalidBecause
import io.kotest.assertions.fail
import io.kotest.assertions.throwables.shouldThrow
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
            verifyValue(Any(), ValidationRule.pass)
        }

        validationScope.status shouldBe Valid
    }

    "Single rule validation..." - {
        "...returns validation status" {
            val scope = ValidationScope()

            val status = scope.verifyValue("v1", failingRule)

            status.shouldBeInvalidBecause("v1".failed { withMessage("v1") })
        }

        "...statuses are merged into the scope's status" {
            val status = ValidationScope().apply {
                verifyValue("v1", failingRule)
                verifyValue("v2", failingRule)
                verifyValue("v3", failingRule)
            }.status

            status.shouldBeInvalidBecause(
                "v1".failed { withMessage("v1") },
                "v2".failed { withMessage("v2") },
                "v3".failed { withMessage("v3") },
            )
        }
    }

    "Block validation..." - {
        "...returns status of all internal operations" {
            val status = ValidationScope().validate {
                verifyValue("v1", failingRule)
                verifyValue("v2", failingRule)
            }

            status.shouldBeInvalidBecause(
                "v1".failed { withMessage("v1") },
                "v2".failed { withMessage("v2") },
            )
        }

        "...statuses are merged into the scope's status" {
            val status = ValidationScope().apply {
                validate { verifyValue("v1", failingRule) }
                validate { verifyValue("v2", failingRule) }
                validate { verifyValue("v3", failingRule) }
            }.status

            status.shouldBeInvalidBecause(
                "v1".failed { withMessage("v1") },
                "v2".failed { withMessage("v2") },
                "v3".failed { withMessage("v3") },
            )
        }

        "...under different path..." - {
            "...works" {
                forAll(nestingCases()) {
                    val expectedViolation =
                        "v".failed {
                            underPath { root + this@forAll }
                            withMessage("v")
                        }
                    val scope = ValidationScope()

                    val nestedStatus = when (this) {
                        is Segment, is Index -> {
                            scope.validate(this) { verifyValue("v", failingRule) }
                        }
                        else -> fail("Unexpected element")
                    }

                    nestedStatus.shouldBeInvalidBecause(expectedViolation)
                    scope.status.shouldBeInvalidBecause(expectedViolation)
                }
            }

            "...must not allow of duplicate paths" {
                val segment = Segment(!"seg1")
                val scope = ValidationScope()
                scope.validate(segment) { verifyValue(Any(), failingRule) }

                shouldThrow<NamingUniquenessException> {
                    scope.validate(segment) { verifyValue(Any(), failingRule) }
                }
            }
        }
    }
})

private fun nestingCases(): Exhaustive<ValidationPath.Element> =
    Exhaustive.of(
        Segment(Arb.notBlankString(maxSize = 10).next()),
        Arb.positiveInt(max = 10).next().asIndex(),
        Arb.notBlankString(maxSize = 10).next().asKey(),
    )
