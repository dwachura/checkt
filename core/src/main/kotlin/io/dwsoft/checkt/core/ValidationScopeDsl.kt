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
     * enclosed into contextual scope, used to validate value, that is a receiver
     * of this function.
     *
     * [Validation block][validation] (containing validation logic) runs in a context
     * of the new scope and [validation DSL][ValidationScopeDsl].
     *
     * @throws [IllegalArgumentException] when [namedAs] is blank.
     */
    context (ValidationScope)
    operator fun <T> T.invoke(
        namedAs: String,
        validation: context (ValidationScope, ValidationScopeDsl) T.() -> Unit,
    ): ValidationScope {
        require(namedAs.isNotBlank()) { "Name cannot be blank for enclosed scopes" }
        return validateInEnclosedScope(NamingPath.Segment.Name(namedAs), validation)
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

    /**
     * Returns a list of [indexed][NamingPath.Segment.Index] [scopes][ValidationScope]
     * enclosed into contextual scope opened for each element contained into iterable that
     * is a receiver of this function.
     *
     * New scopes are indexed according to their position in the iterable.
     *
     * [Validation block][validation] (containing validation logic) runs in a context of
     * the new scope and [validation DSL][ValidationScopeDsl]. Index of validated element
     * is passed to the block via parameter.
     */
    context (ValidationScope)
    fun <T> Iterable<T>.eachElement(
        validation: context (ValidationScope, ValidationScopeDsl) T.(index: Int) -> Unit,
    ): List<ValidationScope> =
        mapIndexed { idx, value ->
            val indexSegment = NamingPath.Segment.Index(idx)
            enclose(indexSegment).apply {
                validation(ValidationScopeDsl(), value, idx)
            }
        }

    /**
     * Returns a map of [indexed][NamingPath.Segment.Index] [scopes][ValidationScope]
     * enclosed into contextual scope opened for each entry of the map that is passed
     * a receiver of this function.
     *
     * New scopes are indexed with entry key transformed by the given [transforming function]
     * [indexedUsingKeysTransformedBy].
     *
     * [Validation block][validation] (containing validation logic) runs in a context of
     * the new scope and [validation DSL][ValidationScopeDsl].
     */
    context (ValidationScope)
    fun <K, V> Map<K, V>.eachEntry(
        indexedUsingKeysTransformedBy: (key: K) -> String,
        validation: context (ValidationScope, ValidationScopeDsl) Map.Entry<K, V>.() -> Unit,
    ): Map<K, ValidationScope> =
        map {
            val key = it.key
            val indexSegment = NamingPath.Segment.Index(indexedUsingKeysTransformedBy(key))
            val newScope = it.validateInEnclosedScope(indexSegment, validation)
            key to newScope
        }.toMap()
}
