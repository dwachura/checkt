package io.dwsoft.checkt.core

/**
 * Groups [validation rules][ValidationRule] under a common [name][validationPath].
 *
 * In general, scopes creates a named context under which validation rules are
 * applied to the values, as well as provides a validation DSL ([verifyValue] and
 * related functions) to create relatively named sub-scopes.
 * This can be used to represent a structure of a complex values being validated
 * (e.g. to provide information about validation of nested properties).
 *
 * [ValidationScope] instances are mutable, as the results of sub-scopes are
 * merged into outer scope's result and thus multithreaded usage of them should be
 * avoided.
 */
class ValidationScope(val validationPath: ValidationPath = ValidationPath()) {
    val status: ValidationStatus
        get() {
            val thisStatus = violations.toValidationStatus()
            val enclosedStatus = enclosedFailures.takeIf { it.isNotEmpty() }
                ?.reduce { v1, v2 -> v1 + v2 }
                ?: ValidationStatus.Valid
            return thisStatus + enclosedStatus
        }

    // TODO: thread-safety
    private val violations = mutableListOf<Violation<*, *, *>>()
    private val enclosedFailures = mutableListOf<ValidationStatus.Invalid>()
    private val enclosedPathElements = mutableListOf<ValidationPath.Element>()

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
        if (status is ValidationStatus.Invalid) enclosedFailures += status
    }

    /**
     * Executes passed [validation block][block] under:
     *
     *  * this scope's [naming][validationPath], if the [passed element][newName]
     *  is null (default behavior)
     *  * a new scope with a relative [naming][validationPath] created
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
                if (enclosedPathElements.contains(newName)) {
                    throw NamingUniquenessException(newPath)
                }
                enclosedPathElements += newName
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
