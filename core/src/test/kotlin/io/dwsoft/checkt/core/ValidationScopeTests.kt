package io.dwsoft.checkt.core

import io.dwsoft.checkt.testing.alwaysFailingRule
import io.dwsoft.checkt.testing.alwaysPassingRule
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeInstanceOf

class ValidationScopeTests : StringSpec({
    "scope of successful validation results with success" {
        val validationScope = ValidationScope()

        with(validationScope) {
            Any().checkAgainst(alwaysPassingRule)
        }

        validationScope.result shouldBe ValidationResult.Success
    }

    "result of failed validation contains all errors" {
        val validationScope = ValidationScope()
        val failingRule = alwaysFailingRule(errorMessage { "$value" })
        val validatedValues = listOf("v1", "v2")
        val expectedErrors = validatedValues.map {
            ValidationError(it, failingRule.context, NamingPath.Empty, it.toDisplayed())
        }

        with(validationScope) {
            validatedValues.forEach {
                it.checkAgainst(failingRule)
            }
        }

        validationScope.result.shouldBeInstanceOf<ValidationResult.Failure>()
            .errors shouldContainExactlyInAnyOrder expectedErrors
    }

    "result of a scope is combined of results of scopes it encloses " {
        val validationScope = ValidationScope()
        val enclosingScopeName = "enclosing".toDisplayed()
        val enclosing = validationScope.enclose(enclosingScopeName)
        val failingRule = alwaysFailingRule(errorMessage { "$value" })
        val checkedValue = Any()

        with(enclosing) {
            checkedValue.checkAgainst(failingRule)
        }

        validationScope.result.shouldBeInstanceOf<ValidationResult.Failure>()
            .errors.shouldContainExactly(
                ValidationError(
                    checkedValue,
                    failingRule.context,
                    enclosingScopeName.toNamingPath(),
                    checkedValue.toString().toDisplayed()
                )
            )
    }

    "nested scopes must have unique names" {
        val scope = ValidationScope()
        scope.enclose("nested".toDisplayed())

        shouldThrow<IllegalArgumentException> { scope.enclose("nested".toDisplayed()) }
            .message shouldContain "Scope named 'nested' is already enclosed"
    }
})
