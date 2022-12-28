package io.dwsoft.checkt.core

import io.dwsoft.checkt.testing.shouldBeValid
import io.dwsoft.checkt.testing.shouldFailWith
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.throwable.shouldHaveMessage

class ValidationResultTests : FreeSpec({
    "Recoverable exceptions are caught" {
        val result = validateCatching {
            throw RuntimeException("test")
        }

        result.shouldFailWith<RuntimeException>()
            .shouldHaveMessage("test")
    }

    "Unrecoverable exceptions are thrown" {
        class UnrecoverableException : RuntimeException()
        Checkt.Settings.ValidationResult.customUnrecoverableErrorTypes =
            listOf(UnrecoverableException::class)
        val expectedException = UnrecoverableException()

        shouldThrow<UnrecoverableException> {
            validateCatching { throw expectedException }
        } shouldBe expectedException
    }

    "Exceptional result can be recovered" {
        val failure = validateCatching { throw RuntimeException() }

        val result = failure.runWhenFailureOfType<RuntimeException> {
            validateCatching { ValidationStatus.Valid }
        }

        result.shouldBeValid()
    }
})
