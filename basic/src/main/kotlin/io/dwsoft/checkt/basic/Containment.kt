package io.dwsoft.checkt.basic

import io.dwsoft.checkt.core.Check
import io.dwsoft.checkt.core.LazyErrorDetails
import io.dwsoft.checkt.core.ValidationRule
import io.dwsoft.checkt.core.toValidationRule

class ContainsAny<V>(elements: Collection<V>) :
    Check<Collection<V>, ContainsAny.Params<V>, ContainsAny<V>>
{
    private val elements = elements.toSet()
    override val params = Params(this.elements)

    override fun invoke(value: Collection<V>): Boolean =
        value.any { elements.contains(it) }

    data class Params<V>(val elements: Collection<V>) : Check.Params<ContainsAny<V>>()
}

fun <V> containAnyOf(
    vararg elements: V,
    errorDetails: LazyErrorDetails<ContainsAny<V>, Collection<V>, ContainsAny.Params<V>> =
        { "${validationPath()} must contain any of ${validationParams.elements}" },
): ValidationRule<ContainsAny<V>, Collection<V>, ContainsAny.Params<V>> =
    ContainsAny(elements.toList()).toValidationRule(errorDetails)

class ContainsAll<V>(elements: Collection<V>) :
    Check<Collection<V>, ContainsAll.Params<V>, ContainsAll<V>>
{
    override val params = Params(elements)
    private val elementQuantities = countDistinct(elements)

    override fun invoke(value: Collection<V>): Boolean = value.containsAllElements()

    private fun Collection<V>.containsAllElements(): Boolean =
        countDistinct(this).let { validatedElementQuantities ->
            elementQuantities.all { (element, minimalRequiredCount) ->
                (validatedElementQuantities[element] ?: 0) >= minimalRequiredCount
            }
        }

    private fun countDistinct(collection: Collection<V>): Map<V, Int> {
        return collection.groupingBy { it }.eachCount()
    }

    data class Params<V>(val elements: Collection<V>) : Check.Params<ContainsAll<V>>()
}

fun <V> containAllOf(
    vararg elements: V,
    errorDetails: LazyErrorDetails<ContainsAll<V>, Collection<V>, ContainsAll.Params<V>> =
        { "${validationPath()} must contain all of ${validationParams.elements}" },
): ValidationRule<ContainsAll<V>, Collection<V>, ContainsAll.Params<V>> =
    ContainsAll(elements.toList()).toValidationRule(errorDetails)
