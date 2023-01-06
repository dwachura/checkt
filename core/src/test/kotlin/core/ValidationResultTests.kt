package io.dwsoft.checkt.core

import io.dwsoft.checkt.core.ValidationStatus.Valid
import io.dwsoft.checkt.testing.shouldBeValid
import io.dwsoft.checkt.testing.shouldFailWith
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.throwable.shouldHaveMessage
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk

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

    "Error recovery" - {
        "Exceptional result can be recovered" {
            val failure = validateCatching { throw RuntimeException() }

            val result = failure.catch(
                FallbackOf<RuntimeException> { Valid }
            )

            result.shouldBeValid()
        }

        "The first fallback supporting exception is called" {
            class SpecificException : RuntimeException()
            val exception = SpecificException()
            val failure = validateCatching { throw exception }
            val fallbackMock = FallbackOf<RuntimeException>(
                mockk { coEvery { this@mockk(any()) } returns Valid }
            )

            val result = failure.catch(
                fallbackMock,
                mockk<FallbackOf<SpecificException>>(),
            )

            result.shouldBeValid()
            coVerify(exactly = 1) { fallbackMock.func(exception) }
        }
    }
})
