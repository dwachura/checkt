package io.dwsoft.checkt.core.checks

import io.dwsoft.checkt.core.Check
import io.dwsoft.checkt.core.LazyErrorMessage
import io.dwsoft.checkt.core.ParameterizedCheck
import io.dwsoft.checkt.core.ParamsOf
import io.dwsoft.checkt.core.ValidationRule
import io.dwsoft.checkt.core.ValidationRules
import io.dwsoft.checkt.core.and
import io.dwsoft.checkt.core.key

class ContainsAny<V>(elements: Collection<V>) :
    ParameterizedCheck<Collection<V>, ContainsAny.Params<V>> by (
            Params(elements.toSet()) and { it.intersect(this.elements).isNotEmpty() }
    )
{
    data class Params<V>(val elements: Set<V>) : ParamsOf<ContainsAny<V>, Params<V>>

    class RuleDescriptor<V> : ValidationRule.Descriptor<Collection<V>, ContainsAny<V>>(Check.key())
}

fun <T, T2 : Collection<T>> ValidationRules<T2>.containsAnyOf(
    elements: T2,
    errorMessage: LazyErrorMessage<ContainsAny.RuleDescriptor<T>, T2, ContainsAny<T>> =
        { "Collection must contain any of elements specified" },
): ValidationRule<ContainsAny.RuleDescriptor<T>, T2, ContainsAny<T>> =
    ContainsAny(elements).toValidationRule(ContainsAny.RuleDescriptor(), errorMessage)

class ContainsAll<V>(private val elements: Collection<V>) :
    ParameterizedCheck<Collection<V>, ContainsAll.Params<V>>
{
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

    class RuleDescriptor<V> : ValidationRule.Descriptor<Collection<V>, ContainsAll<V>>(Check.key())
}

fun <T, T2 : Collection<T>> ValidationRules<T2>.containsAllOf(
    elements: T2,
    errorMessage: LazyErrorMessage<ContainsAll.RuleDescriptor<T>, T2, ContainsAll<T>> =
        { "Collection must contain all of elements specified" },
): ValidationRule<ContainsAll.RuleDescriptor<T>, T2, ContainsAll<T>> =
    ContainsAll(elements).toValidationRule(ContainsAll.RuleDescriptor(), errorMessage)

class ContainsNone<V>(elements: Collection<V>) :
    ParameterizedCheck<Collection<V>, ContainsNone.Params<V>> by (
            Params(elements.toSet()) and { it.intersect(this.elements).isEmpty() }
    )
{
    data class Params<V>(val elements: Set<V>) : ParamsOf<ContainsNone<V>, Params<V>>

    class RuleDescriptor<V> : ValidationRule.Descriptor<Collection<V>, ContainsNone<V>>(Check.key())
}

fun <T, T2 : Collection<T>> ValidationRules<T2>.containsNoneOf(
    elements: T2,
    errorMessage: LazyErrorMessage<ContainsNone.RuleDescriptor<T>, T2, ContainsNone<T>> =
        { "Collection must not contain any of elements specified" },
): ValidationRule<ContainsNone.RuleDescriptor<T>, T2, ContainsNone<T>> =
    ContainsNone(elements).toValidationRule(ContainsNone.RuleDescriptor(), errorMessage)
