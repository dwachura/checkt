package io.dwsoft.checkt.testing

import io.dwsoft.checkt.core.Check
import io.dwsoft.checkt.core.ValidationPath
import io.dwsoft.checkt.core.ValidationResult
import io.dwsoft.checkt.core.ValidationSpec
import io.dwsoft.checkt.core.checkKey
import io.dwsoft.checkt.core.joinToString
import io.dwsoft.checkt.core.unnamed
import io.dwsoft.checkt.core.validate
import io.kotest.assertions.asClue
import io.kotest.assertions.assertSoftly
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

suspend fun <T> testValidation(
    of: T,
    with: ValidationSpec<T>,
    assertions: ValidationTestCase<T>.() -> Unit
) {
    with.validate(of).also {
        ValidationTestCase(of, it).assertions()
    }
}

data class ValidationTestCase<T>(val validated: T, val result: Result<ValidationResult>)

fun Result<ValidationResult>.shouldRepresentCompletedValidation(): ValidationResult =
    "Validation failed due to unexpected error".asClue {
        shouldNotThrowAny { getOrThrow() }
    }

fun ValidationResult.shouldFailBecause(vararg violations: Violation<*>) {
    shouldBeInstanceOf<ValidationResult.Failure>()
        .errors.also { errors ->
            assertSoftly {
                violations.forEach { violation ->
                    val (expectedValue, expectedPath) = violation
                    val expectedCheckType = violation.check.shortIdentifier
                    val readableExpectedPath = expectedPath.joinToString()
                    val maybeError = errors.firstOrNull {
                        it.validationContext.path == expectedPath
                                && it.validatedValue == expectedValue
                    }
                    val error =
                        "Violation of $expectedCheckType by value '$expectedValue' on path '$readableExpectedPath' not found"
                            .asClue { maybeError.shouldNotBeNull() }
                    "Asserting error message of $expectedCheckType violation on path '$readableExpectedPath' (value: '$expectedValue')"
                        .asClue { violation.errorMessageAssertions(error.errorDetails) }
                }
            }
        }
}

data class Violation<T : Check<*, *, *>>(
    val value: Any?,
    val path: ValidationPath,
    val check: Check.Key<T>,
    val errorMessageAssertions: (String) -> Unit,
)

inline fun <reified T : Check<*, *, *>> Any?.violated(
    noinline underPath: ValidationPathBuilder = { ValidationPath.unnamed },
    noinline withMessageThat: (String) -> Unit = {},
): Violation<T> =
    Violation(this, validationPath(underPath), T::class.checkKey(), withMessageThat)

inline fun <reified T : Check<*, *, *>> Any?.violated(
    noinline underPath: ValidationPathBuilder = { ValidationPath.unnamed },
    withMessage: String,
): Violation<T> =
    Violation(this, validationPath(underPath), T::class.checkKey()) { it shouldBe withMessage }
