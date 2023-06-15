package io.dwsoft.checkt.core.checks

import io.dwsoft.checkt.core.Check
import io.dwsoft.checkt.core.LazyErrorMessage
import io.dwsoft.checkt.core.ValidationRule
import io.dwsoft.checkt.core.ValidationRules

object NotEmpty : Check<Container> by Check({ it.isNotEmpty() }) {
    class Rule<V> : ValidationRule.Descriptor<V, NotEmpty, Rule<V>> {
        override val defaultMessage: LazyErrorMessage<Rule<V>, V> = { "Value must not be empty" }
    }
}

@JvmName("notEmptyCharSequence")
fun ValidationRules<CharSequence>.notEmpty(): ValidationRule<NotEmpty.Rule<CharSequence>, CharSequence> =
    ValidationRule.create(NotEmpty.Rule(), NotEmpty, Container::from)

@JvmName("notEmptyCollection")
fun <T> ValidationRules<Collection<*>>.notEmpty(): ValidationRule<NotEmpty.Rule<Collection<T>>, Collection<T>> =
    ValidationRule.create(NotEmpty.Rule(), NotEmpty, Container::from)

@JvmName("notEmptyArray")
fun <T> ValidationRules<Array<*>>.notEmpty(): ValidationRule<NotEmpty.Rule<Array<T>>, Array<T>> =
    ValidationRule.create(NotEmpty.Rule(), NotEmpty, Container::from)

object Empty : Check<Container> by Check({ it.isEmpty() }) {
    class Rule<V> : ValidationRule.Descriptor<V, Empty, Rule<V>> {
        override val defaultMessage: LazyErrorMessage<Rule<V>, V> = { "Value must be empty" }
    }
}

@JvmName("emptyCharSequence")
fun ValidationRules<CharSequence>.empty(): ValidationRule<Empty.Rule<CharSequence>, CharSequence> =
    ValidationRule.create(Empty.Rule(), Empty, Container::from)

@JvmName("emptyCollection")
fun <T> ValidationRules<Collection<*>>.empty(): ValidationRule<Empty.Rule<Collection<T>>, Collection<T>> =
    ValidationRule.create(Empty.Rule(), Empty, Container::from)

@JvmName("emptyArray")
fun <T> ValidationRules<Array<*>>.empty(): ValidationRule<Empty.Rule<Array<T>>, Array<T>> =
    ValidationRule.create(Empty.Rule(), Empty, Container::from)

/**
 * Interface used to group types that can have "empty" state, introduced due to lack of
 * built-in equivalent.
 *
 * New implementation should be added in a form of factory function defined as an extension of
 * [the companion object][Container.Companion] of this interface (see examples for predefined
 * implementations [Container.Companion.from]) together with corresponding [rule][ValidationRule]
 * factory function (e.g. [ValidationRules.empty]).
 */
fun interface Container {
    fun isEmpty(): Boolean

    companion object
}

fun Container.isNotEmpty() = !isEmpty()

fun Container.Companion.from(text: CharSequence): Container =
    Container { text.isEmpty() }

fun <T> Container.Companion.from(collection: Collection<T>): Container =
    Container { collection.isEmpty() }

fun <T> Container.Companion.from(array: Array<T>): Container =
    Container { array.isEmpty() }

