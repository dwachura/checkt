package io.dwsoft.checkt.core.checks

import io.dwsoft.checkt.core.Check
import io.dwsoft.checkt.core.LazyErrorMessage
import io.dwsoft.checkt.core.ValidationRule
import io.dwsoft.checkt.core.ValidationRules
import io.dwsoft.checkt.core.key

object NotEmpty : Check<Container> by Check({ it.isNotEmpty() }) {
    object RuleDescriptor : ValidationRule.Descriptor<Any?, NotEmpty>(Check.key())
}

@JvmName("notEmptyCharSequence")
fun ValidationRules<CharSequence>.notEmpty(
    errorMessage: LazyErrorMessage<NotEmpty.RuleDescriptor, CharSequence, NotEmpty> =
        { "Value must not be empty" },
) : ValidationRule<NotEmpty.RuleDescriptor, CharSequence, NotEmpty> =
    ValidationRule.create(NotEmpty.RuleDescriptor, NotEmpty, errorMessage, Container::from)

@JvmName("notEmptyCollection")
fun <T> ValidationRules<Collection<*>>.notEmpty(
    errorMessage: LazyErrorMessage<NotEmpty.RuleDescriptor, Collection<T>, NotEmpty> =
        { "Value must not be empty" },
) : ValidationRule<NotEmpty.RuleDescriptor, Collection<T>, NotEmpty> =
    ValidationRule.create(NotEmpty.RuleDescriptor, NotEmpty, errorMessage, Container::from)

@JvmName("notEmptyArray")
fun <T> ValidationRules<Array<*>>.notEmpty(
    errorMessage: LazyErrorMessage<NotEmpty.RuleDescriptor, Array<T>, NotEmpty> =
        { "Value must not be empty" },
) : ValidationRule<NotEmpty.RuleDescriptor, Array<T>, NotEmpty> =
    ValidationRule.create(NotEmpty.RuleDescriptor, NotEmpty, errorMessage, Container::from)

object Empty : Check<Container> by Check({ it.isEmpty() }) {
    object RuleDescriptor : ValidationRule.Descriptor<Any?, Empty>(Check.key())
}

@JvmName("emptyCharSequence")
fun ValidationRules<CharSequence>.empty(
    errorMessage: LazyErrorMessage<Empty.RuleDescriptor, CharSequence, Empty> =
        { "Value must be empty" },
) : ValidationRule<Empty.RuleDescriptor, CharSequence, Empty> =
    ValidationRule.create(Empty.RuleDescriptor, Empty, errorMessage, Container::from)

@JvmName("emptyCollection")
fun <T> ValidationRules<Collection<*>>.empty(
    errorMessage: LazyErrorMessage<Empty.RuleDescriptor, Collection<T>, Empty> =
        { "Value must be empty" },
) : ValidationRule<Empty.RuleDescriptor, Collection<T>, Empty> =
    ValidationRule.create(Empty.RuleDescriptor, Empty, errorMessage, Container::from)

@JvmName("emptyArray")
fun <T> ValidationRules<Array<*>>.empty(
    errorMessage: LazyErrorMessage<Empty.RuleDescriptor, Array<T>, Empty> =
        { "Value must be empty" },
) : ValidationRule<Empty.RuleDescriptor, Array<T>, Empty> =
    ValidationRule.create(Empty.RuleDescriptor, Empty, errorMessage, Container::from)

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

