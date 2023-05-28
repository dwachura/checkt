package io.dwsoft.checkt.testing

import io.dwsoft.checkt.core.ValidationResult
import io.dwsoft.checkt.core.ValidationSpecification
import io.dwsoft.checkt.core.ValidationStatus
import io.dwsoft.checkt.core.getOrThrow
import io.kotest.assertions.asClue
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe

suspend fun <T> testValidation(
    of: T,
    with: ValidationSpecification<T>,
    assertions: ValidationTestCase<T>.() -> Unit
) {
    with(of, null).also {
        ValidationTestCase(of, it).assertions()
    }
}

data class ValidationTestCase<T>(val validated: T, val result: ValidationResult)

inline fun <reified T : Throwable> ValidationResult.shouldFailWith(): T =
    "Validation should because of ${T::class.simpleName} exception".asClue {
        shouldThrow { getOrThrow() }
    }

inline fun <reified T : Throwable> ValidationResult.shouldFailWith(exception: T) =
    shouldFailWith<T>() shouldBe exception

fun ValidationResult.shouldRepresentCompletedValidation(): ValidationStatus =
    "Validation failed due to unexpected error".asClue {
        shouldNotThrowAny { getOrThrow() }
    }

fun ValidationResult.shouldBeValid() =
    shouldRepresentCompletedValidation().shouldBeValid()

fun ValidationResult.shouldBeInvalid(withViolationsCountEqualTo: Int? = null) =
    shouldRepresentCompletedValidation().shouldBeInvalid(withViolationsCountEqualTo)

fun ValidationResult.shouldBeInvalidBecause(
    vararg expectedViolations: ExpectedViolation<*, *>
) = shouldRepresentCompletedValidation().shouldBeInvalidBecause(*expectedViolations)

fun ValidationResult.shouldBeInvalidExactlyBecause(
    vararg expectedViolations: ExpectedViolation<*, *>
) = shouldRepresentCompletedValidation().shouldBeInvalidExactlyBecause(*expectedViolations)
