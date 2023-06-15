package io.dwsoft.checkt.testing

import io.dwsoft.checkt.core.ValidationPath
import io.dwsoft.checkt.core.ValidationRule
import io.dwsoft.checkt.core.ValidationStatus
import io.dwsoft.checkt.core.Violation
import io.dwsoft.checkt.core.joinToString
import io.dwsoft.checkt.core.name
import io.dwsoft.checkt.core.toValidationStatus
import io.kotest.assertions.asClue
import io.kotest.assertions.assertSoftly
import io.kotest.assertions.withClue
import io.kotest.inspectors.forAny
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.types.shouldBeInstanceOf

fun ValidationStatus.shouldBeValid() {
    "Validation should be successful".asClue {
        shouldBeInstanceOf<ValidationStatus.Valid>()
    }
}

fun ValidationStatus.shouldBeInvalid(withViolationsCountEqualTo: Int? = null): ValidationStatus.Invalid =
    shouldBeInstanceOf<ValidationStatus.Invalid>()
        .apply {
            withViolationsCountEqualTo?.let {
                "Validation should be invalid because of $withViolationsCountEqualTo violation(s)".asClue {
                    violations shouldHaveSize withViolationsCountEqualTo
                }
            }
        }

fun ValidationStatus.shouldBeInvalidBecause(vararg expectedViolations: ExpectedViolation<*>) =
    shouldBeInvalidBecause(expectedViolations.toList(), false)

fun ValidationStatus.shouldBeInvalidExactlyBecause(vararg expectedViolations: ExpectedViolation<*>) =
    shouldBeInvalidBecause(expectedViolations.toList(), true)

private fun ValidationStatus.shouldBeInvalidBecause(
    expectedViolations: List<ExpectedViolation<*>>,
    checkViolationsCount: Boolean,
): ValidationStatus.Invalid =
    shouldBeInvalid(if (checkViolationsCount) expectedViolations.size else null)
        .apply {
            assertSoftly {
                expectedViolations.forEach { expectedViolation ->
                    val (expectedValue, expectedPath) = expectedViolation
                    val expectedRule = expectedViolation.rule
                    val readableExpectedPath = expectedPath.joinToString()
                    val violationsOnPath = withClue(
                        "Violation of ${expectedRule.name} by value '$expectedValue' " +
                                "on path '$readableExpectedPath' not found"
                    ) {
                        "Actual violations: ${this.debugString()}".asClue {
                            violations.filter {
                                it.context.path == expectedPath
                                        && it.context.descriptor == expectedRule
                                        && it.value == expectedValue
                            }.shouldNotBeEmpty()
                        }
                    }
                    withClue(
                        "Asserting error message of ${expectedRule.name} violation on path " +
                                "'$readableExpectedPath' (value: '$expectedValue')"
                    ) {
                        "Actual: ${violationsOnPath.debugString()}".asClue {
                            violationsOnPath.forAny {
                                expectedViolation.errorMessageAssertions(it.errorMessage)
                            }
                        }
                    }
                }
            }
        }

data class ExpectedViolation<D : ValidationRule.Descriptor<*, *, *>>(
    val value: Any?,
    val path: ValidationPath,
    val rule: D,
    val errorMessageAssertions: (String) -> Unit,
)

fun <D : ValidationRule.Descriptor<*, *, *>> Any?.violated(
    descriptor: D,
    configuration: ViolationAssertionsDsl.() -> Unit,
): ExpectedViolation<D> =
    ViolationAssertionsDsl().apply(configuration).let {
        ExpectedViolation(
            value = this,
            path = it.expectedValidationPath,
            rule = descriptor,
            errorMessageAssertions = it.errorMessageAssertions
        )
    }

fun Any?.failed(configuration: ViolationAssertionsDsl.() -> Unit): ExpectedViolation<AlwaysFailing.Rule> =
    violated(AlwaysFailing.Rule, configuration)

fun ValidationStatus.debugString() =
    when (this) {
        is ValidationStatus.Valid -> ValidationStatus.Valid::class.simpleName
        is ValidationStatus.Invalid -> this.violations.joinToString { it.debugString() }
    }

private fun Collection<Violation<*, *>>.debugString() = toList().toValidationStatus().debugString()
