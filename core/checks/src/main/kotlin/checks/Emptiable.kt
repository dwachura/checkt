package io.dwsoft.checkt.core.checks

import io.dwsoft.checkt.core.Check
import io.dwsoft.checkt.core.LazyErrorMessage
import io.dwsoft.checkt.core.ValidationRule
import io.dwsoft.checkt.core.ValidationRules

object NotEmpty : Check<Emptiable> by Check({ it.isNotEmpty() })

@JvmName("notEmptyCharSequence")
fun ValidationRules<CharSequence>.notEmpty(
    errorMessage: LazyErrorMessage<NotEmpty, CharSequence> = { "Value must not be empty" },
) : ValidationRule<CharSequence, NotEmpty> =
    ValidationRule.Companion.create(NotEmpty, errorMessage) { Emptiable.of(it) }

@JvmName("notEmptyCollection")
fun <T> ValidationRules<Collection<*>>.notEmpty(
    errorMessage: LazyErrorMessage<NotEmpty, Collection<T>> = { "Collection must not be empty" },
) : ValidationRule<Collection<T>, NotEmpty> =
    ValidationRule.Companion.create(NotEmpty, errorMessage) { Emptiable.of(it) }

@JvmName("notEmptyArray")
fun <T> ValidationRules<Array<*>>.notEmpty(
    errorMessage: LazyErrorMessage<NotEmpty, Array<T>> = { "Array must not be empty" },
) : ValidationRule<Array<T>, NotEmpty> =
    ValidationRule.Companion.create(NotEmpty, errorMessage) { Emptiable.of(it) }

object Empty : Check<Emptiable> by Check({ it.isEmpty() })

@JvmName("emptyCharSequence")
fun ValidationRules<CharSequence>.empty(
    errorMessage: LazyErrorMessage<Empty, CharSequence> = { "Value must be empty" },
) : ValidationRule<CharSequence, Empty> =
    ValidationRule.Companion.create(Empty, errorMessage) { Emptiable.of(it) }

@JvmName("emptyCollection")
fun <T> ValidationRules<Collection<*>>.empty(
    errorMessage: LazyErrorMessage<Empty, Collection<T>> = { "Collection must be empty" },
) : ValidationRule<Collection<T>, Empty> =
    ValidationRule.Companion.create(Empty, errorMessage) { Emptiable.of(it) }

@JvmName("emptyArray")
fun <T> ValidationRules<Array<*>>.empty(
    errorMessage: LazyErrorMessage<Empty, Array<T>> = { "Array must be empty" },
) : ValidationRule<Array<T>, Empty> =
    ValidationRule.Companion.create(Empty, errorMessage) { Emptiable.of(it) }

/**
 * Interface used to group types that can have "empty" state, introduced due to lack of
 * built-in equivalent.
 *
 * New implementation should be added in a form of factory function defined as an extension of
 * [the companion object][Emptiable.Companion] of this interface (see examples for predefined
 * implementations [Emptiable.Companion.of]) together with corresponding [rule][ValidationRule]
 * factory function (e.g. [ValidationRules.empty]).
 */
fun interface Emptiable {
    fun isEmpty(): Boolean

    companion object
}

fun Emptiable.isNotEmpty() = !isEmpty()

fun Emptiable.Companion.of(text: CharSequence): Emptiable =
    Emptiable { text.isEmpty() }

fun <T> Emptiable.Companion.of(collection: Collection<T>): Emptiable =
    Emptiable { collection.isEmpty() }

fun <T> Emptiable.Companion.of(array: Array<T>): Emptiable =
    Emptiable { array.isEmpty() }

