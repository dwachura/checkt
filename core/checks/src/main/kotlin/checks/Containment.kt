package io.dwsoft.checkt.core.checks

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
