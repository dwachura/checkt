package io.dwsoft.checkt.core

import io.dwsoft.checkt.testing.failWithMessage
import io.dwsoft.checkt.testing.failed
import io.dwsoft.checkt.testing.pass
import io.dwsoft.checkt.testing.shouldBeInvalidBecause
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

// TODO: refactor to more concise tests ???

class ValidationScopeTests : FreeSpec({
    val failingRule = ValidationRule.failWithMessage { "$value" }

    "Scope of successful validation results with success" {
        val validationScope = ValidationScope(ValidationPath(!"seg1"))

        with(validationScope) {
            validate(Any(), ValidationRule.pass)
        }

        validationScope.result shouldBe ValidationResult.Success
    }

    "Single rule validation..." - {
        "...returns result" {
            val scope = ValidationScope()

            val result = scope.validate("v1", failingRule)

            result.shouldBeInvalidBecause("v1".failed(withMessage = "v1"))
        }

        "...results are merged into the scope's result" {
            val scopeResult = ValidationScope().apply {
                validate("v1", failingRule)
                validate("v2", failingRule)
                validate("v3", failingRule)
            }.result

            scopeResult.shouldBeInvalidBecause(
                "v1".failed(withMessage = "v1"),
                "v2".failed(withMessage = "v2"),
                "v3".failed(withMessage = "v3"),
            )
        }
    }

    "Block validation..." - {
        "...returns result of all internal operations" {
            val scope = ValidationScope()

            val result = scope.validate {
                validate("v1", failingRule)
                validate("v2", failingRule)
            }

            result.shouldBeInvalidBecause(
                "v1".failed(withMessage = "v1"),
                "v2".failed(withMessage = "v2"),
            )
        }

        "...results are merged into the scope's result" {
            val scopeResult = ValidationScope().apply {
                validate { validate("v1", failingRule) }
                validate { validate("v2", failingRule) }
                validate { validate("v3", failingRule) }
            }.result

            scopeResult.shouldBeInvalidBecause(
                "v1".failed(withMessage = "v1"),
                "v2".failed(withMessage = "v2"),
                "v3".failed(withMessage = "v3"),
            )
        }

        "...can be nested under different paths" {
            val (rootViolation, rootPath) =
                ValidationPath().let { "v1".failed(withMessage = "v1", underPath = { it }) to it }
            val (namedScopeViolation, namedScopePath) =
                (rootPath + !"scope1").let { "v2".failed(withMessage = "v2", underPath = { it }) to it }
            val (indexedScopeViolation, indexedScopePath) =
                (namedScopePath + 0.asIndex()).let { "v3".failed(withMessage = "v3", underPath = { it }) to it }
            val keyedScopeViolation =
                "v4".failed(withMessage = "v4", underPath = { indexedScopePath + (!"key").asKey() })
            var sub1Result: ValidationResult? = null
            var indexedScopeResult: ValidationResult? = null
            var keyedScopeResult: ValidationResult? = null

            val rootScopeResult = ValidationScope(rootPath).validate {
                validate("v1", failingRule)
                validate(!"scope1") {
                    validate("v2", failingRule)
                    validate(0.asIndex()) {
                        validate("v3", failingRule)
                        validate((!"key").asKey()) {
                            validate("v4", failingRule)
                        }.also { keyedScopeResult = it }
                    }.also { indexedScopeResult = it }
                }.also { sub1Result = it }
            }

            keyedScopeResult.shouldNotBeNull()
                .shouldBeInvalidBecause(keyedScopeViolation)
            indexedScopeResult.shouldNotBeNull()
                .shouldBeInvalidBecause(indexedScopeViolation, keyedScopeViolation)
            sub1Result.shouldNotBeNull()
                .shouldBeInvalidBecause(namedScopeViolation, indexedScopeViolation, keyedScopeViolation)
            rootScopeResult.shouldBeInvalidBecause(
                rootViolation,
                namedScopeViolation,
                indexedScopeViolation,
                keyedScopeViolation,
            )
        }
    }
})
