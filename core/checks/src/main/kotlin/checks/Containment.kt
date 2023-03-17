package io.dwsoft.checkt.core.checks

import io.dwsoft.checkt.core.Check
import io.dwsoft.checkt.core.LazyErrorMessage
import io.dwsoft.checkt.core.ParameterizedCheck
import io.dwsoft.checkt.core.ParamsOf
import io.dwsoft.checkt.core.ValidationRule
import io.dwsoft.checkt.core.ValidationRules
import io.dwsoft.checkt.core.and

class ContainsAny<V>(elements: Collection<V>) :
    ParameterizedCheck<Collection<V>, ContainsAny.Params<V>> by (
            Params(elements.toSet()) and { it.intersect(this.elements).isNotEmpty() }
            ) {
    data class Params<V>(val elements: Set<V>) : ParamsOf<ContainsAny<V>, Params<V>>
}

fun <T, T2 : Collection<T>> ValidationRules<T2>.containsAnyOf(
    elements: T2,
    errorMessage: LazyErrorMessage<ContainsAny<T>, T2> =
        { "Collection must contain any of elements specified" },
): ValidationRule<T2, ContainsAny<T>> =
    ContainsAny(elements).toValidationRule(errorMessage)

class ContainsAll<V>(private val elements: Collection<V>) :
    ParameterizedCheck<Collection<V>, ContainsAll.Params<V>> {
    private val elementQuantities = countDistinct(elements)

    override suspend fun invoke(value: Collection<V>) =
        ParameterizedCheck.Result(
            value.containsAllElements(),
            Params(elements)
        )

    private fun Collection<V>.containsAllElements(): Boolean =
        countDistinct(this).let { validatedElementQuantities ->
            elementQuantities.all { (element, minimalRequiredCount) ->
                (validatedElementQuantities[element] ?: 0) >= minimalRequiredCount
            }
        }

    private fun countDistinct(collection: Collection<V>): Map<V, Int> {
        return collection.groupingBy { it }.eachCount()
    }

    data class Params<V>(val elements: Collection<V>) :
        ParamsOf<ContainsAll<V>, Params<V>>
}

fun <T, T2 : Collection<T>> ValidationRules<T2>.containsAllOf(
    elements: T2,
    errorMessage: LazyErrorMessage<ContainsAll<T>, T2> =
        { "Collection must contain all of elements specified" },
): ValidationRule<T2, ContainsAll<T>> =
    ContainsAll(elements).toValidationRule(errorMessage)

class ContainsNone<V>(elements: Collection<V>) :
    ParameterizedCheck<Collection<V>, ContainsNone.Params<V>> by (
            Params(elements.toSet()) and { it.intersect(this.elements).isEmpty() }
            ) {
    data class Params<V>(val elements: Set<V>) : ParamsOf<ContainsNone<V>, Params<V>>
}

fun <T, T2 : Collection<T>> ValidationRules<T2>.containsNoneOf(
    elements: T2,
    errorMessage: LazyErrorMessage<ContainsNone<T>, T2> =
        { "Collection must not contain any of elements specified" },
): ValidationRule<T2, ContainsNone<T>> =
    ContainsNone(elements).toValidationRule(errorMessage)

object ContainsAnything : Check<Collection<*>> by Check({ it.isNotEmpty() })

fun <T, T2 : Collection<T>> ValidationRules<T2>.containsAnything(
    errorMessage: LazyErrorMessage<ContainsAnything, T2> =
        { "Collection must contain anything" },
): ValidationRule<T2, ContainsAnything> =
    ContainsAnything.toValidationRule(errorMessage)

object ContainsNothing : Check<Collection<*>> by Check({ it.isEmpty() })

fun <T, T2 : Collection<T>> ValidationRules<T2>.containsNothing(
    errorMessage: LazyErrorMessage<ContainsNothing, T2> =
        { "Collection must not contain any elements" },
): ValidationRule<T2, ContainsNothing> =
    ContainsNothing.toValidationRule(errorMessage)

// TODO: test + move to other file

class NotEmpty<V : Any> private constructor() : Check<V> by Check({
    when (it) {
        is String -> it.isEmpty()
        is Collection<*> -> it.isEmpty()
        is Array<*> -> it.isEmpty()
        else -> throw IllegalArgumentException(
            "Unsupported type: ${it::class.qualifiedName}"
        )
    }
}) {
    companion object {
        fun text(): NotEmpty<CharSequence> = NotEmpty()

        fun <T> collectionOf(): NotEmpty<Collection<T>> = NotEmpty()

        fun <T> arrayOf(): NotEmpty<Array<T>> = NotEmpty()
    }
}

@JvmName("notEmptyCharSequence")
fun ValidationRules<CharSequence>.notEmpty(
    errorMessage: LazyErrorMessage<NotEmpty<CharSequence>, CharSequence> =
        { "Value must not be empty" },
) : ValidationRule<CharSequence, NotEmpty<CharSequence>> =
    NotEmpty.text().toValidationRule(errorMessage)

@JvmName("notEmptyCollection")
fun <T> ValidationRules<Collection<T>>.notEmpty(
    errorMessage: LazyErrorMessage<NotEmpty<Collection<T>>, Collection<T>> =
        { "Collection must not be empty" },
) : ValidationRule<Collection<T>, NotEmpty<Collection<T>>> =
    NotEmpty.collectionOf<T>().toValidationRule(errorMessage)

@JvmName("notEmptyArray")
fun <T> ValidationRules<Array<T>>.notEmpty(
    errorMessage: LazyErrorMessage<NotEmpty<Array<T>>, Array<T>> =
        { "Array must not be empty" },
) : ValidationRule<Array<T>, NotEmpty<Array<T>>> =
    NotEmpty.arrayOf<T>().toValidationRule(errorMessage)
