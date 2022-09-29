package io.dwsoft.checkt.core

import io.dwsoft.checkt.core.NamingPath.Segment.Name
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
    "Scope of successful validation results with success" {
        val validationScope = ValidationScope()

        with(validationScope) {
            Any().checkAgainst(alwaysPassingRule)
        }

        validationScope.result shouldBe ValidationResult.Success
    }

    "Result of failed validation contains all errors" {
        val validationScope = ValidationScope()
        val failingRule = alwaysFail { "$value" }
        val validatedValues = listOf("v1", "v2")
        val expectedErrors = validatedValues.map {
            ValidationError(it, failingRule.context, NamingPath.unnamed, it)
        }

        with(validationScope) {
            validatedValues.forEach {
                it.checkAgainst(failingRule)
            }
        }

        validationScope.result.shouldBeInstanceOf<ValidationResult.Failure>()
            .errors shouldContainExactlyInAnyOrder expectedErrors
    }

    "Result of a scope is combined of results of scopes it encloses" {
        val validationScope = ValidationScope()
        val enclosedScope1 = validationScope.enclose(Name("enclosed1"))
        val enclosedScope2 = validationScope.enclose(Name("enclosed2"))
        val enclosedResult1 = enclosedScope1.apply {
            Any().checkAgainst(alwaysFailingRule)
        }.result
        val enclosedResult2 = enclosedScope2.apply {
            Any().checkAgainst(alwaysFailingRule)
        }.result

        val enclosingScopeResult = validationScope.result

        enclosingScopeResult shouldBe (enclosedResult1 + enclosedResult2)
    }

    "Nested scopes must have unique names" {
        val name = Name("name")
        val scope = ValidationScope()
        scope.enclose(name)

        shouldThrow<IllegalArgumentException> { scope.enclose(name) }
            .message shouldContain "Scope named ${name.value} is already enclosed"
    }

    "Value cannot be validated by equal rules into single scope" {
        shouldThrow<IllegalStateException> {
            val value = Any()
            ValidationScope().apply {
                value.checkAgainst(alwaysFailingRule)
                value.checkAgainst(alwaysFailingRule)
            }
        }.message shouldContain "Value .* violated the same check .* twice".toRegex()
    }
})
