package io.dwsoft.checkt.core

import io.dwsoft.checkt.core.ValidationResult.Failure
import io.dwsoft.checkt.core.ValidationResult.Success
import io.kotest.core.spec.style.StringSpec
import io.kotest.data.blocking.forAll
import io.kotest.data.row
import io.kotest.matchers.shouldBe
import io.mockk.mockk

class ValidationResultTests : StringSpec({
    "merge of successes is success" {
        Success plus Success shouldBe Success
    }

    "merge of success and failure results in failure" {
        val failure = Failure(validationError())
        forAll(
            row(Success, failure),
            row(failure, Success),
        ) { r1, r2 ->
            r1 plus r2 shouldBe failure
        }
    }

    "merge of failures is failure containing both error sets" {
        val failure1 = Failure(validationError())
        val failure2 = Failure(validationError(), validationError())
        forAll(
            row(failure1, failure2),
            row(failure2, failure1),
        ) { r1, r2 ->
            r1 plus r2 shouldBe Failure(r1.errors + r2.errors)
        }
    }
})

private fun validationError() = mockk<ValidationError<*, *, *>>()
