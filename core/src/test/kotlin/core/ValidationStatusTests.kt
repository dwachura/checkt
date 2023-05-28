package io.dwsoft.checkt.core

import io.dwsoft.checkt.core.ValidationStatus.Invalid
import io.dwsoft.checkt.core.ValidationStatus.Valid
import io.dwsoft.checkt.testing.forAll
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Exhaustive
import io.kotest.property.exhaustive.of
import io.mockk.mockk

class ValidationStatusTests : StringSpec({
    "Statuses are merged" {
        forAll(statusMergingCases()) {
            when {
                left is Valid && right is Valid ->
                    left + right shouldBe Valid
                left is Valid && right is Invalid ->
                    left + right shouldBe invalid(withViolationsOf = right)
                left is Invalid && right is Valid ->
                    left + right shouldBe invalid(withViolationsOf = left)
                left is Invalid && right is Invalid ->
                    left + right shouldBe invalid(withViolationsOf = listOf(left, right))
            }
        }
    }
})

private fun statusMergingCases(): Exhaustive<MergingCase> =
    Exhaustive.of(
        Valid to Valid,
        Valid to invalid(),
        invalid() to Valid,
        invalid() to invalid()
    )

private data class MergingCase(
    val left: ValidationStatus,
    val right: ValidationStatus,
)

private infix fun ValidationStatus.to(right: ValidationStatus): MergingCase =
    MergingCase(this, right)

private fun invalid(withViolationsOf: List<Invalid> = emptyList()): Invalid =
    when {
        withViolationsOf.isEmpty() -> Invalid(violation())
        else -> Invalid(withViolationsOf.flatMap { it.violations })
    }

private fun invalid(withViolationsOf: Invalid): Invalid = invalid(listOf(withViolationsOf))

private fun violation() = mockk<Violation<*, *, *>>()
