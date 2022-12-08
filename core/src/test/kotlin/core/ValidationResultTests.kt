package io.dwsoft.checkt.core

import io.dwsoft.checkt.core.ValidationResult.Failure
import io.dwsoft.checkt.core.ValidationResult.Success
import io.dwsoft.checkt.testing.forAll
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Exhaustive
import io.kotest.property.exhaustive.of
import io.mockk.mockk

class ValidationResultTests : StringSpec({
    "Results are merged" {
        forAll(resultMergingCases()) {
            when {
                left is Success && right is Success ->
                    left + right shouldBe Success
                left is Success && right is Failure ->
                    left + right shouldBe failure(withErrorsOf = right)
                left is Failure && right is Success ->
                    left + right shouldBe failure(withErrorsOf = left)
                left is Failure && right is Failure ->
                    left + right shouldBe failure(withErrorsOf = listOf(left, right))
            }
        }
    }
})

private fun resultMergingCases(): Exhaustive<MergingCase> =
    Exhaustive.of(
        Success to Success,
        Success to failure(),
        failure() to Success,
        failure() to failure()
    )

private data class MergingCase(
    val left: ValidationResult,
    val right: ValidationResult,
)

private infix fun ValidationResult.to(right: ValidationResult): MergingCase =
    MergingCase(this, right)

private fun failure(withErrorsOf: List<Failure> = emptyList()): Failure =
    when {
        withErrorsOf.isEmpty() -> Failure(validationError())
        else -> Failure(withErrorsOf.flatMap { it.errors })
    }

private fun failure(withErrorsOf: Failure): Failure = failure(listOf(withErrorsOf))

private fun validationError() = mockk<ValidationError<*, *, *>>()
