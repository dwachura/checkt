package io.dwsoft.checkt.testing

import io.dwsoft.checkt.core.Check
import io.dwsoft.checkt.core.ValidationPath
import io.dwsoft.checkt.core.ValidationStatus
import io.dwsoft.checkt.core.Violation
import io.dwsoft.checkt.core.checkKey
import io.dwsoft.checkt.core.joinToString
import io.dwsoft.checkt.core.toValidationStatus
import io.kotest.assertions.asClue
import io.kotest.assertions.assertSoftly
import io.kotest.assertions.withClue
import io.kotest.inspectors.forAny
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlin.reflect.KClass

fun ValidationStatus.shouldBeValid() {
    "Validation should be successful".asClue {
        shouldBeInstanceOf<ValidationStatus.Valid>()
    }
}

fun ValidationStatus.shouldBeInvalid(
    withViolationsCountEqualTo: Int? = null
): ValidationStatus.Invalid =
    shouldBeInstanceOf<ValidationStatus.Invalid>()
        .apply {
            withViolationsCountEqualTo?.let {
                "Validation should be invalid because of $withViolationsCountEqualTo violation(s)"
                    .asClue { violations shouldHaveSize withViolationsCountEqualTo }
            }
        }

fun ValidationStatus.shouldBeInvalidBecause(
    vararg expectedViolations: ExpectedViolation<*>,
) = shouldBeInvalidBecause(expectedViolations.toList(), false)

fun ValidationStatus.shouldBeInvalidExactlyBecause(
    vararg expectedViolations: ExpectedViolation<*>,
) = shouldBeInvalidBecause(expectedViolations.toList(), true)

private fun ValidationStatus.shouldBeInvalidBecause(
    expectedViolations: List<ExpectedViolation<*>>,
    checkViolationsCount: Boolean,
): ValidationStatus.Invalid =
    shouldBeInvalid(if (checkViolationsCount) expectedViolations.size else null)
        .apply {
            assertSoftly {
                expectedViolations.forEach { expectedViolation ->
                    val (expectedValue, expectedPath) = expectedViolation
                    val expectedCheck = expectedViolation.check
                    val readableExpectedPath = expectedPath.joinToString()
                    val violationsOnPath = withClue(
                        "Violation of ${expectedCheck.shortIdentifier} by value '$expectedValue' " +
                                "on path '$readableExpectedPath' not found"
                    ) {
                        "Actual violations: ${this.debugString()}".asClue {
                            violations.filter {
                                it.context.path == expectedPath
                                        && it.context.key == expectedCheck
                                        && it.value == expectedValue
                            }.shouldNotBeEmpty()
                        }
                    }
                    withClue(
                        "Asserting error message of ${expectedCheck.shortIdentifier} violation on path " +
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

data class ExpectedViolation<T : Check<*>>(
    val value: Any?,
    val path: ValidationPath,
    val check: Check.Key<T>,
    val errorMessageAssertions: (String) -> Unit,
)

inline fun <reified T : Check<*>> Any?.violated(
    noinline configuration: ViolationAssertionsDsl.() -> Unit,
): ExpectedViolation<T> =
    violated(T::class, configuration)

fun <T : Check<*>> Any?.violated(
    checkType: KClass<T>,
    configuration: ViolationAssertionsDsl.() -> Unit,
): ExpectedViolation<T> =
    ViolationAssertionsDsl().apply(configuration).let {
        ExpectedViolation(
            value = this,
            path = it.expectedValidationPath,
            check = checkType.checkKey(),
            errorMessageAssertions = it.errorMessageAssertions
        )
    }

fun Any?.failed(
    configuration: ViolationAssertionsDsl.() -> Unit,
): ExpectedViolation<AlwaysFailingCheck> =
    violated(AlwaysFailingCheck::class, configuration)

fun Violation<*, *>.debugString() =
    buildString {
        append("{ ")
        append("check: '${context.key.shortIdentifier}', ")
        append("path: '${context.path.joinToString()}', ")
        append("value: '$value', ")
        append("message: '${errorMessage}', ")
        append(" }")
    }

fun ValidationStatus.debugString() =
    when (this) {
        is ValidationStatus.Valid -> ValidationStatus.Valid::class.simpleName
        is ValidationStatus.Invalid -> this.violations.joinToString { it.debugString() }
    }

fun Collection<Violation<*, *>>.debugString() = toList().toValidationStatus().debugString()
