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
    namedAs: NonBlankString? = null,
    validation: context (ValidationScope, ValidationScopeDsl) T.() -> Unit,
): ValidationScope =
    when (namedAs) {
        null -> ValidationScope()
        else -> ValidationScope(ValidationPath.Segment.Name(namedAs))
    }.apply {
        validation(ValidationScopeDsl(), value)
    }

class ValidationScopeDsl {
    class Named<T>(val value: T, val name: NonBlankString)

    fun <T> T.namedAs(name: NonBlankString): Named<T> = Named(this, name)

    /**
     * Opens and returns a new, named [scope][ValidationScope] enclosed into contextual
     * scope, used to validate [value, taken from the receiver of this function][Named.value].
     * The new scope's name is [taken from the receiver as well][Named.name].
     *
     * [Validation block][validation] (containing validation logic) runs in a context
     * of the new scope and [validation DSL][ValidationScopeDsl].
     */
    context (ValidationScope)
    infix fun <T> Named<T>.requireTo(
        validation: context (ValidationScope, ValidationScopeDsl) T.() -> Unit,
    ): ValidationScope =
        value.validateInEnclosedScope(ValidationPath.Segment.Name(name), validation)

    context (ValidationScope)
    private fun <T> T.validateInEnclosedScope(
        namedAs: ValidationPath.Segment,
        validation: context (ValidationScope, ValidationScopeDsl) T.() -> Unit,
    ): ValidationScope =
        enclose(namedAs).apply {
            validation(ValidationScopeDsl(), this@validateInEnclosedScope)
        }

    /**
     * "Overloaded" version of [Named.requireTo] function that takes property as
     * a receiver. A new scope's name and value to validate is taken from the property
     * specified ([KProperty0.name] and [KProperty0.get] respectively).
     */
    context (ValidationScope)
    operator fun <T> KProperty0<T>.invoke(
        validation: context (ValidationScope, ValidationScopeDsl) T.() -> Unit,
    ): ValidationScope =
        get().namedAs(!name).requireTo(validation)

    /**
     * Alias of [KProperty0.invoke][invoke].
     */
    context (ValidationScope)
    infix fun <T> KProperty0<T>.requireTo(
        validation: context (ValidationScope, ValidationScopeDsl) T.() -> Unit,
    ): ValidationScope =
        this(validation)

    /**
     * Function used to apply given [validation rule][rule] to the validated [value][this].
     *
     * Returns [error][ValidationError] or null if the value conform to the given rule.
     */
    context (ValidationScope)
    infix fun <C : Check<V, P, C>, V : Any?, P : Check.Params<C>> V.must(
        rule: ValidationRule<C, V, P>
    ): ValidationError<C, V, P>? =
        checkAgainst(rule)

    /**
     * Function used to apply given [validation rule][this] to the [value][V], in the context
     * of which the function is called.
     *
     * Returns [error][ValidationError] or null if the value conform to the given rule.
     */
    context (ValidationScope, V)
    operator fun <C : Check<V, P, C>, V : Any?, P : Check.Params<C>>
            ValidationRule<C, V, P>.unaryPlus(): ValidationError<C, V, P>? =
        this@V.checkAgainst(this)

    /**
     * Returns a list of [indexed][ValidationPath.Segment.Index] [scopes][ValidationScope]
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
            val indexSegment = ValidationPath.Segment.Index(idx)
            enclose(indexSegment).apply {
                validation(ValidationScopeDsl(), value, idx)
            }
        }

    /**
     * Returns a map of [indexed][ValidationPath.Segment.Index] [scopes][ValidationScope]
     * enclosed into contextual scope opened for each entry of the map that is passed
     * a receiver of this function.
     *
     * New scopes are indexed with entry key transformed by the given [transforming function]
     * [indexedUsingKeysTransformedBy].
     *
     * Validation blocks (containing validation logic) [for entry key][keyValidation] and
     * [value][valueValidation] run in a context of the new scope and
     * [validation DSL][ValidationScopeDsl]. Validated values are passed to the lambdas as
     * a receivers. Also, both blocks have access to a value from the "opposite" side (value
     * for key, key for value) accessible via lambda's parameter.
     */
    context (ValidationScope)
    fun <K, V> Map<K, V>.eachEntry(
        indexedUsingKeysTransformedBy: (key: K) -> NonBlankString,
        keyValidation: context (ValidationScope, ValidationScopeDsl) K.(value: V) -> Unit = {},
        valueValidation: context (ValidationScope, ValidationScopeDsl) V.(key: K) -> Unit,
    ): Map<K, ValidationScope> =
        map {
            val key = it.key
            val indexSegment = ValidationPath.Segment.Index(indexedUsingKeysTransformedBy(key))
            val newScope = it.validateInEnclosedScope(indexSegment) {
                val entryKey = this.key
                val value = this.value
                enclose(ValidationPath.Segment.Name(!"key")).apply {
                    keyValidation(this, ValidationScopeDsl(), entryKey, value)
                }
                enclose(ValidationPath.Segment.Name(!"value")).apply {
                    valueValidation(this, ValidationScopeDsl(), value, entryKey)
                }
            }
            key to newScope
        }.toMap()
}
