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
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

fun ValidationStatus.shouldBeValid() {
    "Validation should be successful".asClue {
        shouldBeInstanceOf<ValidationStatus.Valid>()
    }
}

fun ValidationStatus.shouldBeInvalid() = shouldBeInstanceOf<ValidationStatus.Invalid>()

fun ValidationStatus.shouldBeInvalidBecause(
    vararg expectedViolations: ExpectedViolation<*>
): ValidationStatus.Invalid =
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

fun Violation<*, *, *>.debugString() =
    buildString {
        append("{ ")
        append("check: '${validationContext.key.shortIdentifier}', ")
        append("path: '${validationContext.path.joinToString()}', ")
        append("value: '${validatedValue}', ")
        append("message: '${errorMessage}', ")
        append(" }")
    }

fun ValidationStatus.debugString() =
    when (this) {
        is ValidationStatus.Valid -> ValidationStatus.Valid::class.simpleName
        is ValidationStatus.Invalid -> this.violations.joinToString { it.debugString() }
    }

fun Collection<Violation<*, *, *>>.debugString() = toList().toValidationStatus().debugString()
