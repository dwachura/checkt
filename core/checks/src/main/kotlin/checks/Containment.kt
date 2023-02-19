package io.dwsoft.checkt.core.checks

import io.dwsoft.checkt.core.LazyErrorMessage
import io.dwsoft.checkt.core.ParameterizedCheck
import io.dwsoft.checkt.core.ParamsOf
import io.dwsoft.checkt.core.ValidationRule
import io.dwsoft.checkt.core.ValidationRules
import io.dwsoft.checkt.core.params

class ContainsAny<V>(elements: Collection<V>) :
    ParameterizedCheck<Collection<V>, ContainsAny.Params<V>>
{
    private val elements = elements.toSet()
    override val params = Params(elements)

    override suspend fun invoke(value: Collection<V>): Boolean =
        value.any { elements.contains(it) }

    data class Params<V>(val elements: Collection<V>) :
        ParamsOf<ContainsAny<V>, Params<V>>
}

fun <T, T2 : Collection<T>> ValidationRules<T2>.containsAnyOf(
    elements: T2,
    errorMessage: LazyErrorMessage<ContainsAny<T>, T2> =
        { "Value must contain any of ${context.params.elements}" },
): ValidationRule<ContainsAny<T>, T2> =
    ContainsAny(elements).toValidationRule(errorMessage)

class ContainsAll<V>(elements: Collection<V>) :
    ParameterizedCheck<Collection<V>, ContainsAll.Params<V>>
{
    override val params = Params(elements)
    private val elementQuantities = countDistinct(elements)

    override suspend fun invoke(value: Collection<V>): Boolean =
        value.containsAllElements()

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
        { "Value must contain all of ${context.params.elements}" },
): ValidationRule<ContainsAll<T>, T2> =
    ContainsAll(elements).toValidationRule(errorMessage)
