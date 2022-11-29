package io.dwsoft.checkt.core

import kotlin.reflect.KProperty0

// TODO: correct docks after context removal and receivers refactor

/**
 * Opens and returns a [validation scope][ValidationScope] for a given [value].
 *
 * Validation logic is supposed to be defined into [validation block][validation],
 * which runs into a context of the opened scope and [validation DSL][ValidationSpec].
 */
fun <T> validate(
    value: T,
    namedAs: NonBlankString? = null,
    validation: ValidationSpec<T>.() -> Unit,
): ValidationResult =
    when (namedAs) {
        null -> ValidationScope()
        else -> ValidationScope(ValidationPath.Segment.Name(namedAs))
    }.also { scope ->
        ValidationSpec(value, scope).apply(validation)
    }.result

class Named<T>(val value: T, val name: NonBlankString)

class ValidationSpec<V>(
    val subject: V,
    private val scope: ValidationScope
) {
    fun <T> T.namedAs(name: NonBlankString): Named<T> = Named(this, name)

    /**
     * Opens and returns a new, named [scope][ValidationScope] enclosed into contextual
     * scope, used to validate [value, taken from the receiver of this function][Named.value].
     * The new scope's name is [taken from the receiver as well][Named.name].
     *
     * [Validation block][validation] (containing validation logic) runs in a context
     * of the new scope and [validation DSL][ValidationSpec].
     */
    infix fun <T> Named<T>.requireTo(validation: ValidationSpec<T>.() -> Unit): ValidationScope =
        runEnclosed(value, ValidationPath.Segment.Name(name), validation)

    internal fun <T> runEnclosed(
        value: T,
        namedAs: ValidationPath.Segment,
        validation: (spec: ValidationSpec<T>) -> Unit
    ): ValidationScope =
        scope.enclose(namedAs).also { enclosedScope ->
            validation(ValidationSpec(value, enclosedScope))
        }

    /**
     * "Overloaded" version of [Named.requireTo] function that takes property as
     * a receiver. A new scope's name and value to validate is taken from the property
     * specified ([KProperty0.name] and [KProperty0.get] respectively).
     */
    infix fun <T> KProperty0<T>.requireTo(validation: ValidationSpec<T>.() -> Unit): ValidationScope =
        get().namedAs(!name).requireTo(validation)

    /**
     * Alias of [KProperty0.requireTo].
     */
    operator fun <T> KProperty0<T>.invoke(validation: ValidationSpec<T>.() -> Unit) = requireTo(validation)

    /**
     * Function used to apply given [validation rule][beValidAgainst] to the validated [value][this].
     *
     * Registers and returns [error][ValidationError] if the value doesn't conform to the given rule
     * or returns null otherwise.
     */
    infix fun <C : Check<V, P, C>, V : Any?, P : Check.Params<C>> V.requireTo(
        beValidAgainst: ValidationRule<C, V, P>
    ): ValidationError<C, V, P>? =
        checkValue(this, beValidAgainst)

    /**
     * Shortcut to apply [V.requireTo][Any.requireTo] on a [currently validated value]
     * [subject].
     */
    operator fun <C : Check<V, P, C>, P : Check.Params<C>> ValidationRule<C, V, P>.unaryPlus() =
        subject.requireTo(beValidAgainst = this)

    private fun <C : Check<V, P, C>, V : Any?, P : Check.Params<C>> checkValue(
        value: V,
        beValidAgainst: ValidationRule<C, V, P>
    ): ValidationError<C, V, P>? =
        scope.checkValueAgainstRule(value, beValidAgainst)
}

/**
 * Returns a list of [indexed][ValidationPath.Segment.Index] [scopes][ValidationScope]
 * enclosed into contextual scope opened for each element contained into iterable that
 * is a receiver of this function.
 *
 * New scopes are indexed according to their position in the iterable.
 *
 * [Validation block][validation] (containing validation logic) runs in a context of
 * the new scope and [validation DSL][ValidationSpec]. Index of validated element
 * is passed to the block via parameter.
 */
fun <T : Iterable<V>, V> ValidationSpec<T>.eachElement(
    validation: ValidationSpec<V>.(index: Int) -> Unit,
) {
    subject.mapIndexed { idx, value ->
        runEnclosed(value, ValidationPath.Segment.Index(idx)) { enclosedScope ->
            enclosedScope.validation(idx)
        }
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
 * [validation DSL][ValidationSpec]. Validated values are passed to the lambdas as
 * a receivers. Also, both blocks have access to a value from the "opposite" side (value
 * for key, key for value) accessible via lambda's parameter.
 */
fun <T : Map<K, V>, K, V> ValidationSpec<T>.eachEntry(
    indexedUsingKeysTransformedBy: (key: K) -> NonBlankString,
    keyValidation: ValidationSpec<K>.(value: V) -> Unit = {},
    valueValidation: ValidationSpec<V>.(key: K) -> Unit,
) {
    subject.map { entry ->
        val key = entry.key
        val indexSegment = ValidationPath.Segment.Index(indexedUsingKeysTransformedBy(key))
        val newScope = runEnclosed(entry, indexSegment) { entryScope ->
            val value = entry.value
            entryScope.runEnclosed(key, ValidationPath.Segment.Name(!"key")) { keyScope ->
                keyScope.keyValidation(value)
            }
            entryScope.runEnclosed(value, ValidationPath.Segment.Name(!"value")) { valueScope ->
                valueScope.valueValidation(key)
            }
        }
        key to newScope
    }.toMap()
}
