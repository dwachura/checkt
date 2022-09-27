package io.dwsoft.checkt.basic

import io.dwsoft.checkt.core.Check
import io.dwsoft.checkt.core.Check.Context
import io.dwsoft.checkt.core.ErrorDetailsBuilder
import io.dwsoft.checkt.core.ValidationRule
import io.dwsoft.checkt.core.errorMessage
import io.dwsoft.checkt.core.toValidationRule

class ContainsAny<V>(elements: Collection<V>) :
    Check<Collection<V>, ContainsAny.Key, ContainsAny.Params<V>>
{
    private val elements = elements.toSet()
    override val context = Context.of(Key, Params(this.elements))

    override fun invoke(value: Collection<V>): Boolean =
        value.any { elements.contains(it) }

    object Key : Check.Key
    data class Params<V>(val elements: Collection<V>) : Check.Params()
}

fun <V> containAnyOf(
    vararg elements: V,
    errorDetailsBuilder: ErrorDetailsBuilder<Collection<V>, ContainsAny.Key, ContainsAny.Params<V>> =
        errorMessage { "${validationPath()} must contain any of ${validationParams.elements}" },
): ValidationRule<Collection<V>, ContainsAny.Key, ContainsAny.Params<V>> =
    ContainsAny(elements.toList()).toValidationRule(errorDetailsBuilder)

class ContainsAll<V>(elements: Collection<V>) :
    Check<Collection<V>, ContainsAll.Key, ContainsAll.Params<V>>
{
    override val context = Context.of(Key, Params(elements))
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

    object Key : Check.Key
    data class Params<V>(val elements: Collection<V>) : Check.Params()
}

fun <V> containAllOf(
    vararg elements: V,
    errorDetailsBuilder: ErrorDetailsBuilder<Collection<V>, ContainsAll.Key, ContainsAll.Params<V>> =
        errorMessage { "${validationPath()} must contain all of ${validationParams.elements}" },
): ValidationRule<Collection<V>, ContainsAll.Key, ContainsAll.Params<V>> =
    ContainsAll(elements.toList()).toValidationRule(errorDetailsBuilder)
