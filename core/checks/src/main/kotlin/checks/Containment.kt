package io.dwsoft.checkt.core.checks

import io.dwsoft.checkt.core.Check
import io.dwsoft.checkt.core.LazyErrorMessage
import io.dwsoft.checkt.core.ParameterizedCheck
import io.dwsoft.checkt.core.ParamsOf
import io.dwsoft.checkt.core.ValidationRule
import io.dwsoft.checkt.core.ValidationRules
import io.dwsoft.checkt.core.and
import io.dwsoft.checkt.core.params

class ContainsAny<V>(elements: Collection<V>) :
    ParameterizedCheck<Collection<V>, ContainsAny.Params<V>> by (
            Params(elements.toSet()) and { it.intersect(this.elements).isNotEmpty() }
    )
{
    data class Params<V>(val elements: Set<V>) : ParamsOf<ContainsAny<V>, Params<V>>
}

fun <T, T2 : Collection<T>> ValidationRules<T2>.containsAnyOf(
    elements: T2,
    errorMessage: LazyErrorMessage<ContainsAny<T>, T2> =
        { "Value must contain any of ${context.params.elements}" },
): ValidationRule<T2, ContainsAny<T>> =
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
): ValidationRule<T2, ContainsAll<T>> =
    ContainsAll(elements).toValidationRule(errorMessage)

class ContainsNone<V>(elements: Collection<V>) :
    ParameterizedCheck<Collection<V>, ContainsNone.Params<V>> by (
            Params(elements.toSet()) and { it.intersect(this.elements).isEmpty() }
    )
{
    data class Params<V>(val elements: Set<V>) : ParamsOf<ContainsNone<V>, Params<V>>
}

fun <T, T2 : Collection<T>> ValidationRules<T2>.containsNoneOf(
    elements: T2,
    errorMessage: LazyErrorMessage<ContainsNone<T>, T2> =
        { "Value must not contain any of ${context.params.elements}" },
): ValidationRule<T2, ContainsNone<T>> =
    ContainsNone(elements).toValidationRule(errorMessage)

object ContainsAnything : Check<Collection<*>> by Check({ it.isNotEmpty() })

fun <T, T2 : Collection<T>> ValidationRules<T2>.containsAnything(
    errorMessage: LazyErrorMessage<ContainsAnything, T2> =
        { "Value must contain anything" },
): ValidationRule<T2, ContainsAnything> =
    ContainsAnything.toValidationRule(errorMessage)

object ContainsNothing : Check<Collection<*>> by Check({ it.isEmpty() })

fun <T, T2 : Collection<T>> ValidationRules<T2>.containsNothing(
    errorMessage: LazyErrorMessage<ContainsNothing, T2> =
        { "Value must not contain any elements" },
): ValidationRule<T2, ContainsNothing> =
    ContainsNothing.toValidationRule(errorMessage)


