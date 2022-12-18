package io.dwsoft.checkt.testing

import io.dwsoft.checkt.core.Check
import io.dwsoft.checkt.core.ValidationPath
import io.dwsoft.checkt.core.ValidationResult
import io.dwsoft.checkt.core.ValidationSpecification
import io.dwsoft.checkt.core.Violation
import io.dwsoft.checkt.core.checkKey
import io.dwsoft.checkt.core.joinToString
import io.dwsoft.checkt.core.toValidationResult
import io.kotest.assertions.asClue
import io.kotest.assertions.assertSoftly
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.withClue
import io.kotest.inspectors.forAny
import io.kotest.inspectors.forSingle
import io.kotest.inspectors.runTests
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

suspend fun <T> testValidation(
    of: T,
    with: ValidationSpecification<T>,
    assertions: ValidationTestCase<T>.() -> Unit
) {
    with(of, null).also {
        ValidationTestCase(of, it).assertions()
    }
}

data class ValidationTestCase<T>(val validated: T, val result: Result<ValidationResult>)

fun ValidationResult.shouldPass() {
    "Validation should be successful".asClue {
        shouldBeInstanceOf<ValidationResult.Success>()
    }
}

fun ValidationResult.shouldBeInvalid() = shouldBeInstanceOf<ValidationResult.Failure>()

fun ValidationResult.shouldBeInvalidBecause(
    vararg expectedViolations: ExpectedViolation<*>
): ValidationResult.Failure =
    shouldBeInvalid()
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
                                it.validationContext.path == expectedPath
                                        && it.validationContext.key == expectedCheck
                                        && it.validatedValue == expectedValue
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

data class ExpectedViolation<T : Check<*, *, *>>(
    val value: Any?,
    val path: ValidationPath,
    val check: Check.Key<T>,
    val errorMessageAssertions: (String) -> Unit,
)

inline fun <reified T : Check<*, *, *>> Any?.violated(
    noinline underPath: ValidationPathBuilder? = null,
    noinline withMessageThat: (String) -> Unit = {},
): ExpectedViolation<T> =
    ExpectedViolation(
        value = this,
        path = path((underPath ?: { root })),
        check = T::class.checkKey(),
        errorMessageAssertions = withMessageThat
    )

inline fun <reified T : Check<*, *, *>> Any?.violated(
    noinline underPath: ValidationPathBuilder? = null,
    withMessage: String,
): ExpectedViolation<T> =
    violated(underPath) { it shouldBe withMessage }

fun Any?.failed(
    underPath: ValidationPathBuilder? = null,
    withMessage: String,
): ExpectedViolation<AlwaysFailingCheck> =
    violated(underPath, withMessage)

fun Result<ValidationResult>.shouldRepresentCompletedValidation(): ValidationResult =
    "Validation failed due to unexpected error".asClue {
        shouldNotThrowAny { getOrThrow() }
    }

fun Result<ValidationResult>.shouldBeInvalid() =
    shouldRepresentCompletedValidation().shouldBeInvalid()

fun Result<ValidationResult>.shouldBeInvalidBecause(
    vararg expectedViolations: ExpectedViolation<*>
) = shouldRepresentCompletedValidation().shouldBeInvalidBecause(*expectedViolations)

fun Result<ValidationResult>.shouldPass() =
    shouldRepresentCompletedValidation().shouldPass()

fun Violation<*, *, *>.debugString() =
    buildString {
        append("{ ")
        append("check: '${validationContext.key.shortIdentifier}', ")
        append("path: '${validationContext.path.joinToString()}', ")
        append("value: '${validatedValue}', ")
        append("message: '${errorMessage}', ")
        append(" }")
    }

fun ValidationResult.debugString() =
    when (this) {
        is ValidationResult.Success -> ValidationResult.Success::class.simpleName
        is ValidationResult.Failure -> this.violations.joinToString { it.debugString() }
    }

fun Collection<Violation<*, *, *>>.debugString() = toList().toValidationResult().debugString()
