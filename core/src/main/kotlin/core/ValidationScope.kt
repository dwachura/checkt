package io.dwsoft.checkt.core

import io.dwsoft.checkt.core.ValidationPath.Segment.Index

class ValidationScope(val validationPath: ValidationPath = ValidationPath()){
    val result: ValidationResult
        get() {
            val thisResult = violations.toValidationResult()
            val enclosedResult = enclosedFailures.takeIf { it.isNotEmpty() }
                ?.reduce { v1: ValidationResult, v2: ValidationResult -> v1 + v2 }
                ?: ValidationResult.Success
            return thisResult + enclosedResult
        }

    private val violations: MutableList<Violation<*, *, *>> = mutableListOf()
    private val enclosedFailures: MutableList<ValidationResult.Failure> = mutableListOf()

    /**
     * Verifies [value] against passed [condition][rule].
     *
     * Eventual [violation][Violation] is saved into internal structures of
     * a scope and returned.
     */
    suspend fun <C : Check<V, P, C>, V, P : Check.Params<C>> validate(
        value: V,
        rule: ValidationRule<C, V, P>,
    ): ValidationResult =
        rule.verify(value)(validationPath)
            .toValidationResult()
            .also { merge(it) }

    private fun merge(result: ValidationResult) {
        if (result is ValidationResult.Failure) enclosedFailures += result
    }

    suspend fun validate(block: suspend ValidationScope.() -> Unit): ValidationResult =
        applyBlockIntoNewScope(validationPath, block)

    suspend fun validate(
        name: NotBlankString,
        block: suspend ValidationScope.() -> Unit,
    ) = applyBlockIntoNewScope(validationPath + name, block)

    suspend fun validate(
        index: Index,
        block: suspend ValidationScope.() -> Unit,
    ) = applyBlockIntoNewScope(validationPath + index, block)

    private suspend fun applyBlockIntoNewScope(
        validationPath: ValidationPath,
        validation: suspend ValidationScope.() -> Unit,
    ): ValidationResult =
        ValidationScope(validationPath)
            .apply { validation() }
            .result
            .also { merge(it) }
}
