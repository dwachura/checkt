package io.dwsoft.checkt.core

import io.dwsoft.checkt.core.ValidationPath.Segment.Name
import io.dwsoft.checkt.testing.alwaysFailWithMessage
import io.dwsoft.checkt.testing.alwaysFail
import io.dwsoft.checkt.testing.alwaysPass
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeInstanceOf

class ValidationScopeTests : StringSpec({
    "Scope of successful validation results with success" {
        val validationScope = ValidationScope()

        with(validationScope) {
            checkValueAgainstRule(Any(), alwaysPass)
        }

        validationScope.result shouldBe ValidationResult.Success
    }

    "Result of failed validation contains all errors" {
        val validationScope = ValidationScope()
        val failingRule = alwaysFailWithMessage { "$value" }
        val validatedValues = listOf("v1", "v2")
        val errorContext = ValidationError.Context(
            failingRule.check, ValidationPath.unnamed
        )
        val expectedErrors = validatedValues.map {
            ValidationError(it, errorContext, it)
        }

        with(validationScope) {
            validatedValues.forEach {
                checkValueAgainstRule(it, failingRule)
            }
        }

        validationScope.result.shouldBeInstanceOf<ValidationResult.Failure>()
            .errors shouldContainExactlyInAnyOrder expectedErrors
    }

    "Result of a scope is combined of results of scopes it encloses" {
        val validationScope = ValidationScope()
        val enclosedScope1 = validationScope.enclose(Name(!"enclosed1"))
        val enclosedScope2 = validationScope.enclose(Name(!"enclosed2"))
        val enclosedResult1 = enclosedScope1.apply {
            checkValueAgainstRule(Any(), alwaysFail)
        }.result
        val enclosedResult2 = enclosedScope2.apply {
            checkValueAgainstRule(Any(), alwaysFail)
        }.result

        val enclosingScopeResult = validationScope.result

        enclosingScopeResult shouldBe (enclosedResult1 + enclosedResult2)
    }

    "Nested scopes must have unique names" {
        val name = Name(!"name")
        val scope = ValidationScope()
        scope.enclose(name)

        shouldThrow<IllegalArgumentException> { scope.enclose(name) }
            .message shouldContain "Scope named ${name.value} is already enclosed"
    }
})
