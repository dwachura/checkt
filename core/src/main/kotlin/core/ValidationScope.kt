package io.dwsoft.checkt.core

import io.dwsoft.checkt.core.ValidationPath.Segment.Index

/**
 * Groups [validation rules][ValidationRule] under a common [name][validationPath].
 *
 * In general, scopes creates a named context under which validation rules are
 * applied to the values, as well as provides a validation DSL ([validate] and
 * related functions) to create relatively named sub-scopes.
 * This can be used to represent a structure of a complex values being validated
 * (e.g. to provide information about validation of nested properties).
 *
 * [ValidationScope] instances are mutable, as the results of sub-scopes are
 * merged into outer scope's result and thus multithreaded usage of them should is
 * discouraged.
 */
class ValidationScope(val validationPath: ValidationPath = ValidationPath()){
    val status: ValidationStatus
        get() {
            val thisStatus = violations.toValidationStatus()
            val enclosedStatus = enclosedFailures.takeIf { it.isNotEmpty() }
                ?.reduce { v1, v2 -> v1 + v2 }
                ?: ValidationStatus.Valid
            return thisStatus + enclosedStatus
        }

    private val violations = mutableListOf<Violation<*, *, *>>()
    private val enclosedFailures = mutableListOf<ValidationStatus.Invalid>()

    /**
     * Verifies [value] against passed [condition][rule].
     *
     * Eventual [violation][Violation] is saved into internal structures of
     * a scope and returned.
     */
    suspend fun <C : Check<V, P, C>, V, P : Check.Params<C>> validate(
        value: V,
        rule: ValidationRule<C, V, P>,
    ): ValidationScopeBlockReturnable =
        scopeBlock {
            rule.verify(value)(validationPath)
                .toValidationStatus()
                .also { merge(it) }
        }

    private fun merge(status: ValidationStatus) {
        if (status is ValidationStatus.Invalid) enclosedFailures += status
    }

    /**
     * Executes passed [validation block][block] under this scope's
     * [naming][validationPath] and returns [status][ValidationStatus] of this
     * execution.
     */
    suspend fun validate(
        block: suspend ValidationScope.() -> ValidationScopeBlockReturnable
    ): ValidationScopeBlockReturnable =
        applyBlockIntoNewScope(validationPath, block)

    /**
     * Executes passed [validation block][block] under a new scope with a
     * relative [naming][validationPath] created by appending given [name segment]
     * [name] to the path of this scope and returns [status][ValidationStatus]
     * of this execution.
     */
    suspend fun validate(
        name: NotBlankString,
        block: suspend ValidationScope.() -> ValidationScopeBlockReturnable,
    ) = applyBlockIntoNewScope(validationPath + name, block)

    /**
     * Overloaded version of [validate] that runs given [block] into a new scope
     * with a [naming][validationPath] composed of this scope's path and passed
     * [index] segment.
     */
    suspend fun validate(
        index: Index,
        block: suspend ValidationScope.() -> ValidationScopeBlockReturnable,
    ) = applyBlockIntoNewScope(validationPath + index, block)

    private suspend fun applyBlockIntoNewScope(
        validationPath: ValidationPath,
        block: suspend ValidationScope.() -> ValidationScopeBlockReturnable,
    ): ValidationScopeBlockReturnable =
        scopeBlock {
            ValidationScope(validationPath)
                .apply { block() }
                .status
                .also { merge(it) }
        }
}

/**
 * [ValidationStatus] wrapper returned by [ValidationScope] DSL functions
 * (e.g. [validate]) and defined as a return value of a validation blocks passed
 * to those functions.
 *
 * It helps to prevent forgetting to call any of them into validation blocks -
 * language itself enforces callers to return [ValidationScopeBlockReturnable]
 * instance from validation block, which can be only retrieved from validation
 * scope DSL functions.
 */
sealed interface ValidationScopeBlockReturnable {
    val status: ValidationStatus
}

suspend fun ValidationScope.scopeBlock(
    block: suspend ValidationScope.() -> ValidationStatus
): ValidationScopeBlockReturnable =
    ValidationScopeBlockReturnableInternal(block())

@JvmInline
private value class ValidationScopeBlockReturnableInternal(
    override val status: ValidationStatus
) : ValidationScopeBlockReturnable
