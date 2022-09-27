package io.dwsoft.checkt.basic

import io.dwsoft.checkt.core.Check
import io.dwsoft.checkt.core.Check.Context
import io.dwsoft.checkt.core.ErrorDetailsBuilder
import io.dwsoft.checkt.core.ValidationRule
import io.dwsoft.checkt.core.errorMessage
import io.dwsoft.checkt.core.toValidationRule

class LessThan<V>(private val max: V) :
    Check<Comparable<V>, LessThan.Key, LessThan.Params<V>>
{
    override val context = Context.of(Key, Params(max))

    override fun invoke(value: Comparable<V>): Boolean =
        value < max

    object Key : Check.Key
    data class Params<V>(val max: V) : Check.Params()
}

fun <V : Comparable<V>> beLessThan(
    max: V,
    errorDetailsBuilder: ErrorDetailsBuilder<V, LessThan.Key, LessThan.Params<V>> =
        errorMessage { "${validationPath()} must be less than ${validationParams.max}" },
): ValidationRule<V, LessThan.Key, LessThan.Params<V>> =
    LessThan(max).toValidationRule(errorDetailsBuilder)

class LessThanOrEqual<V>(private val max: V) :
    Check<Comparable<V>, LessThanOrEqual.Key, LessThanOrEqual.Params<V>>
{
    override val context = Context.of(Key, Params(max))

    override fun invoke(value: Comparable<V>): Boolean =
        value <= max

    object Key : Check.Key
    data class Params<V>(val max: V) : Check.Params()
}

fun <V : Comparable<V>> notBeGreaterThan(
    max: V,
    errorDetailsBuilder: ErrorDetailsBuilder<V, LessThanOrEqual.Key, LessThanOrEqual.Params<V>> =
        errorMessage { "${validationPath()} must not be greater than ${validationParams.max}" },
): ValidationRule<V, LessThanOrEqual.Key, LessThanOrEqual.Params<V>> =
    LessThanOrEqual(max).toValidationRule(errorDetailsBuilder)

class GreaterThan<V>(private val min: V) :
    Check<Comparable<V>, GreaterThan.Key, GreaterThan.Params<V>>
{
    override val context = Context.of(Key, Params(min))

    override fun invoke(value: Comparable<V>): Boolean =
        value > min

    object Key : Check.Key
    data class Params<V>(val min: V) : Check.Params()
}

fun <V : Comparable<V>> beGreaterThan(
    min: V,
    errorDetailsBuilder: ErrorDetailsBuilder<V, GreaterThan.Key, GreaterThan.Params<V>> =
        errorMessage { "${validationPath()} must be greater than ${validationParams.min}" },
): ValidationRule<V, GreaterThan.Key, GreaterThan.Params<V>> =
    GreaterThan(min).toValidationRule(errorDetailsBuilder)

class GreaterThanOrEqual<V>(private val min: V) :
    Check<Comparable<V>, GreaterThanOrEqual.Key, GreaterThanOrEqual.Params<V>>
{
    override val context = Context.of(Key, Params(min))

    override fun invoke(value: Comparable<V>): Boolean =
        value >= min

    object Key : Check.Key
    data class Params<V>(val min: V) : Check.Params()
}

fun <V : Comparable<V>> notBeLessThan(
    min: V,
    errorDetailsBuilder: ErrorDetailsBuilder<V, GreaterThanOrEqual.Key, GreaterThanOrEqual.Params<V>> =
        errorMessage { "${validationPath()} must not be less than ${validationParams.min}" },
): ValidationRule<V, GreaterThanOrEqual.Key, GreaterThanOrEqual.Params<V>> =
    GreaterThanOrEqual(min).toValidationRule(errorDetailsBuilder)
