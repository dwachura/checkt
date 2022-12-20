package io.dwsoft.checkt.core

import io.dwsoft.checkt.core.ValidationPath.Segment.Index

class ValidationScope(val validationPath: ValidationPath = ValidationPath()){
    val status: ValidationStatus
        get() {
            val thisStatus = violations.toValidationStatus()
            val enclosedStatus = enclosedFailures.takeIf { it.isNotEmpty() }
                ?.reduce { v1, v2 -> v1 + v2 }
                ?: ValidationStatus.Valid
            return thisStatus + enclosedStatus
        }

    private val violations: MutableList<Violation<*, *, *>> = mutableListOf()
    private val enclosedFailures: MutableList<ValidationStatus.Invalid> = mutableListOf()

    /**
     * Verifies [value] against passed [condition][rule].
     *
     * Eventual [violation][Violation] is saved into internal structures of
     * a scope and returned.
     */
    suspend fun <C : Check<V, P, C>, V, P : Check.Params<C>> validate(
        value: V,
        rule: ValidationRule<C, V, P>,
    ): ValidationScopeBlockTermination =
        scopeOperation {
            rule.verify(value)(validationPath)
                .toValidationStatus()
                .also { merge(it) }
        }

    private fun merge(status: ValidationStatus) {
        if (status is ValidationStatus.Invalid) enclosedFailures += status
    }

    suspend fun validate(
        block: suspend ValidationScope.() -> ValidationScopeBlockTermination
    ): ValidationScopeBlockTermination =
        applyBlockIntoNewScope(validationPath, block)

    suspend fun validate(
        name: NotBlankString,
        block: suspend ValidationScope.() -> ValidationScopeBlockTermination,
    ) = applyBlockIntoNewScope(validationPath + name, block)

    suspend fun validate(
        index: Index,
        block: suspend ValidationScope.() -> ValidationScopeBlockTermination,
    ) = applyBlockIntoNewScope(validationPath + index, block)

    private suspend fun applyBlockIntoNewScope(
        validationPath: ValidationPath,
        validation: suspend ValidationScope.() -> ValidationScopeBlockTermination,
    ): ValidationScopeBlockTermination =
        scopeOperation {
            ValidationScope(validationPath)
                .apply { validation() }
                .status
                .also { merge(it) }
        }

    private inline fun scopeOperation(
        block: () -> ValidationStatus,
    ): ValidationScopeBlockTermination =
        ValidationScopeBlockTerminationInternal(block())
}

sealed interface ValidationScopeBlockTermination {
    val status: ValidationStatus
}

fun ValidationStatus.asValidationScopeBlockTermination(): ValidationScopeBlockTermination =
    ValidationScopeBlockTerminationInternal(this)

@JvmInline
private value class ValidationScopeBlockTerminationInternal(
    override val status: ValidationStatus
) : ValidationScopeBlockTermination
