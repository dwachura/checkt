package io.dwsoft.checkt.testing

import io.dwsoft.checkt.core.ValidationResult
import io.dwsoft.checkt.core.ValidationSpecification
import io.dwsoft.checkt.core.ValidationStatus
import io.dwsoft.checkt.core.getOrThrow
import io.kotest.assertions.asClue
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.assertions.withClue
import io.kotest.matchers.shouldBe
import java.lang.Exception

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

fun ValidationResult.shouldBeInvalid() =
    shouldRepresentCompletedValidation().shouldBeInvalid()

fun ValidationResult.shouldBeInvalidBecause(
    vararg expectedViolations: ExpectedViolation<*>
) = shouldRepresentCompletedValidation().shouldBeInvalidBecause(*expectedViolations)

fun ValidationResult.shouldPass() =
    shouldRepresentCompletedValidation().shouldPass()
