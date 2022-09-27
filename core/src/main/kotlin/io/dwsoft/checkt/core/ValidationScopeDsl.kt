package io.dwsoft.checkt.core

import kotlin.reflect.KProperty0

/**
 * Opens and returns a [validation scope][ValidationScope] for a given [value].
 *
 * Validation logic is supposed to be defined into [validation block][validation],
 * which runs into a context of the opened scope and [validation DSL][ValidationScopeDsl].
 */
fun <T> validate(
    value: T,
    namedAs: String? = null,
    validation: context (ValidationScope, ValidationScopeDsl) T.() -> Unit,
): ValidationScope =
    when {
        namedAs.isNullOrBlank() -> ValidationScope()
        else -> ValidationScope(NamingPath.Segment.Name(namedAs))
    }.apply {
        validation(ValidationScopeDsl(), value)
    }

class ValidationScopeDsl {
    /**
     * Opens and returns a new, [named][namedAs] [scope][ValidationScope]
     * enclosed into contextual scope, used to validate value, that is passed to this
     * function as a receiver.
     *
     * Validation block (containing validation logic) runs in a context of the new
     * scope and [validation DSL][ValidationScopeDsl].
     *
     * @throws [IllegalArgumentException] when [namedAs] is blank.
     */
    context (ValidationScope)
    operator fun <T> T.invoke(
        namedAs: String,
        validation: context (ValidationScope, ValidationScopeDsl) T.() -> Unit,
    ): ValidationScope {
        require(namedAs.isNotBlank()) { "Name cannot be blank for enclosed scopes" }
        return validateInEnclosedScope(
            NamingPath.Segment.Name(namedAs),
            validation
        )
    }

    context (ValidationScope)
    private fun <T> T.validateInEnclosedScope(
        namedAs: NamingPath.Segment,
        validation: context (ValidationScope, ValidationScopeDsl) T.() -> Unit,
    ): ValidationScope =
        enclose(namedAs).apply {
            validation(ValidationScopeDsl(), this@validateInEnclosedScope)
        }

    /**
     * "Overloaded" version of [T.invoke][invoke] function that takes property as
     * a receiver. A new scope's name and value to validate is taken from the property
     * specified ([KProperty0.name] and [KProperty0.get] respectively).
     */
    context (ValidationScope)
    operator fun <T> KProperty0<T>.invoke(
        validation: context (ValidationScope, ValidationScopeDsl) T.() -> Unit,
    ): ValidationScope =
        get()(name, validation)

    /**
     * Function used to apply given [validation rule][rule] to the validated [value][this].
     *
     * Returns [error][ValidationError] or null if the value conform to the given rule.
     */
    context (ValidationScope)
    infix fun <V : Any?, K : Check.Key, P : Check.Params> V.must(
        rule: ValidationRule<V, K, P>
    ): ValidationError<V, K, P>? =
        checkAgainst(rule)

    // TODO: kdoc
    context (ValidationScope)
    fun <T> Iterable<T>.eachElement(
        validation: context (ValidationScope, ValidationScopeDsl) T.() -> Unit,
    ) =
        forEachIndexed { idx, value ->
            val indexSegment = NamingPath.Segment.Index(idx.toString())
            value.validateInEnclosedScope(indexSegment, validation)
        }
}
