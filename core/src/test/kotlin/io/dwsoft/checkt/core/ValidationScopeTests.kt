package io.dwsoft.checkt.core

import io.dwsoft.checkt.testing.alwaysFail
import io.dwsoft.checkt.testing.alwaysFailingRule
import io.dwsoft.checkt.testing.alwaysPassingRule
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
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
        val failingRule = alwaysFail(errorMessage { "$value" })
        val validatedValues = listOf("v1", "v2")
        val expectedErrors = validatedValues.map {
            ValidationError(it, failingRule.context, NamingPath.unnamed, it.toDisplayed())
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
        val enclosedScope1 = validationScope.enclose(NamingPath.Segment.Name("enclosed1"))
        val enclosedScope2 = validationScope.enclose(NamingPath.Segment.Name("enclosed2"))
        val enclosedResult1 = enclosedScope1.apply {
            Any().checkAgainst(alwaysFailingRule)
        }.result
        val enclosedResult2 = enclosedScope2.apply {
            Any().checkAgainst(alwaysFailingRule)
        }.result

        val enclosingScopeResult = validationScope.result

        enclosingScopeResult shouldBe (enclosedResult1 + enclosedResult2)
    }

    "nested scopes must have unique names" {
        val scope = ValidationScope()
        scope.enclose(NamingPath.Segment.Name("same-name"))

        shouldThrow<IllegalArgumentException> { scope.enclose(NamingPath.Segment.Name("same-name")) }
            .message shouldContain "Scope named same-name is already enclosed"
    }
})
