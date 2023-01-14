package io.dwsoft.checkt.core

import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * Groups [validation rules][ValidationRule] under a common [name][validationPath].
 *
 * In general, scopes creates a named context under which validation rules are
 * applied to the values, as well as provides a validation DSL ([verifyValue] and
 * related functions) to create relatively named sub-scopes.
 * This can be used to represent a structure of a complex values being validated
 * (e.g. to provide information about validation of nested properties).
 *
 * [ValidationScope] instances are mutable - every [violation][Violation] is stored
 * into internal collection, as well as statuses of sub-scopes to be later merged
 * into outer scope's [status].
 */
class ValidationScope(val validationPath: ValidationPath = ValidationPath()) {
    /**
     * Status of a scope calculated from this scope's direct operations results
     * and statuses of scopes enclosed into it.
     */
    val status: ValidationStatus
        get() {
            val thisStatus = violations.acquireAndRun { toValidationStatus() }
            val enclosedStatus = enclosedFailures.acquireAndRun {
                takeIf { it.isNotEmpty() }
                    ?.reduce { v1, v2 -> v1 + v2 }
                    ?: ValidationStatus.Valid
            }
            return thisStatus + enclosedStatus
        }

    private val violations = Synchronized(
        mutableListOf<Violation<*, *, *>>()
    )
    private val enclosedFailures = Synchronized(
        mutableListOf<ValidationStatus.Invalid>()
    )
    private val enclosedPathElements = Synchronized(
        mutableListOf<ValidationPath.Element>()
    )

    /**
     * Verifies [value] against passed [condition][rule].
     *
     * Eventual [violation][Violation] is saved into internal structures of
     * a scope and returned.
     */
    suspend fun <C : Check<V, P, C>, V, P : Check.Params<C>> verifyValue(
        value: V,
        rule: ValidationRule<C, V, P>,
    ): ValidationStatus =
        rule.verify(value)(validationPath)
            .toValidationStatus()
            .also { merge(it) }

    private fun merge(status: ValidationStatus) {
        if (status is ValidationStatus.Invalid) {
            enclosedFailures.acquireAndRun { this += status }
        }
    }

    /**
     * Executes passed [validation block][block] under:
     *
     *  - this scope's [naming][validationPath], if the [passed element][newName]
     *  is null (default behavior)
     *  - a new scope with a relative [naming][validationPath] created
     *  by appending [given element][newName] to the path of this scope
     *
     * and returns [status][ValidationStatus] of this execution.
     *
     * @throws NamingUniquenessException when scope with requested [name][newName]
     *  has been already enclosed
     */
    suspend fun validate(
        newName: ValidationPath.Element? = null,
        block: suspend ValidationScope.() -> Unit
    ): ValidationStatus =
        when (newName) {
            null -> applyBlockIntoNewScope(validationPath, block)
            else -> {
                val newPath = validationPath + newName
                enclosedPathElements.acquireAndRun {
                    if (contains(newName)) {
                        throw NamingUniquenessException(newPath)
                    }
                    this += newName
                }
                applyBlockIntoNewScope(newPath, block)
            }
        }

    private suspend fun applyBlockIntoNewScope(
        validationPath: ValidationPath,
        block: suspend ValidationScope.() -> Unit,
    ): ValidationStatus =
        ValidationScope(validationPath)
            .apply { block() }
            .status
            .also { merge(it) }

    class NamingUniquenessException(validationPath: ValidationPath) :
        RuntimeException(
            "Scope named '${validationPath.joinToString()}' was already opened"
        )
}

private class Synchronized<T>(private val obj: T) {
    private val lock = ReentrantLock()

    fun <R> acquireAndRun(f: T.() -> R): R =
        lock.withLock { obj.f() }
}
