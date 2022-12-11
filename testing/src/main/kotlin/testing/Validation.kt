package io.dwsoft.checkt.testing

import io.dwsoft.checkt.core.Check
import io.dwsoft.checkt.core.ValidationPath
import io.dwsoft.checkt.core.ValidationResult
import io.dwsoft.checkt.core.ValidationSpecification
import io.dwsoft.checkt.core.checkKey
import io.dwsoft.checkt.core.joinToString
import io.dwsoft.checkt.core.unnamed
import io.kotest.assertions.asClue
import io.kotest.assertions.assertSoftly
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.withClue
import io.kotest.matchers.nulls.shouldNotBeNull
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

fun ValidationResult.shouldBeInvalidBecause(vararg expectedViolations: ExpectedViolation<*>) {
    shouldBeInstanceOf<ValidationResult.Failure>()
        .violations.also { violations ->
            assertSoftly {
                expectedViolations.forEach { expectedViolation ->
                    val (expectedValue, expectedPath) = expectedViolation
                    val expectedCheckType = expectedViolation.check.shortIdentifier
                    val readableExpectedPath = expectedPath.joinToString()
                    val maybeViolation = violations.firstOrNull {
                        it.validationContext.path == expectedPath
                                && it.validatedValue == expectedValue
                    }
                    val violation = withClue(
                        "Violation of $expectedCheckType by value '$expectedValue' " +
                                "on path '$readableExpectedPath' not found"
                    ) {
                        maybeViolation.shouldNotBeNull()
                    }
                    withClue(
                        "Asserting error message of $expectedCheckType violation on path " +
                            "'$readableExpectedPath' (value: '$expectedValue')"
                    ) {
                        expectedViolation.errorMessageAssertions(violation.errorMessage)
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
    noinline underPath: ValidationPathBuilder = { ValidationPath.unnamed },
    noinline withMessageThat: (String) -> Unit = {},
): ExpectedViolation<T> =
    ExpectedViolation(this, validationPath(underPath), T::class.checkKey(), withMessageThat)

inline fun <reified T : Check<*, *, *>> Any?.violated(
    noinline underPath: ValidationPathBuilder? = null,
    withMessage: String,
): ExpectedViolation<T> =
    underPath?.let {
        violated(underPath) { it shouldBe withMessage }
    } ?: violated { it shouldBe withMessage }

fun Any?.failed(
    underPath: ValidationPathBuilder? = null,
    withMessage: String,
): ExpectedViolation<AlwaysFailingCheck> =
    underPath?.let { violated(underPath, withMessage) }
        ?: violated(withMessage = withMessage)

fun Result<ValidationResult>.shouldRepresentCompletedValidation(): ValidationResult =
    "Validation failed due to unexpected error".asClue {
        shouldNotThrowAny { getOrThrow() }
    }

fun Result<ValidationResult>.shouldBeInvalidBecause(
    vararg expectedViolations: ExpectedViolation<*>
) = shouldRepresentCompletedValidation().shouldBeInvalidBecause(*expectedViolations)

fun Result<ValidationResult>.shouldPass() =
    shouldRepresentCompletedValidation().shouldPass()
