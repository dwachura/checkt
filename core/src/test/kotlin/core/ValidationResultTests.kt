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
                    left + right shouldBe failure(withViolationsOf = right)
                left is Failure && right is Success ->
                    left + right shouldBe failure(withViolationsOf = left)
                left is Failure && right is Failure ->
                    left + right shouldBe failure(withViolationsOf = listOf(left, right))
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

private fun failure(withViolationsOf: List<Failure> = emptyList()): Failure =
    when {
        withViolationsOf.isEmpty() -> Failure(violation())
        else -> Failure(withViolationsOf.flatMap { it.violations })
    }

private fun failure(withViolationsOf: Failure): Failure = failure(listOf(withViolationsOf))

private fun violation() = mockk<Violation<*, *, *>>()
