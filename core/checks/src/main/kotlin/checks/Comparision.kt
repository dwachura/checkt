package io.dwsoft.checkt.core.checks

import io.dwsoft.checkt.core.Check
import io.dwsoft.checkt.core.LazyErrorMessage
import io.dwsoft.checkt.core.ValidationRule
import io.dwsoft.checkt.core.toValidationRule

class LessThan<V>(private val max: V) :
    Check<Comparable<V>, LessThan.Params<V>, LessThan<V>>
{
    override val params = Params(max)

    override suspend fun invoke(value: Comparable<V>): Boolean =
        value < max

    data class Params<V>(val max: V) : Check.Params<LessThan<V>>()
}

fun <V : Comparable<V>> beLessThan(
    max: V,
    errorMessage: LazyErrorMessage<LessThan<V>, V, LessThan.Params<V>> =
        { "${validationPath()} must be less than ${validationParams.max}" },
): ValidationRule<LessThan<V>, V, LessThan.Params<V>> =
    LessThan(max).toValidationRule(errorMessage)

class LessThanOrEqual<V>(private val max: V) :
    Check<Comparable<V>, LessThanOrEqual.Params<V>, LessThanOrEqual<V>>
{
    override val params = Params(max)

    override suspend fun invoke(value: Comparable<V>): Boolean =
        value <= max

    data class Params<V>(val max: V) : Check.Params<LessThanOrEqual<V>>()
}

fun <V : Comparable<V>> notBeGreaterThan(
    max: V,
    errorMessage: LazyErrorMessage<LessThanOrEqual<V>, V, LessThanOrEqual.Params<V>> =
        { "${validationPath()} must not be greater than ${validationParams.max}" },
): ValidationRule<LessThanOrEqual<V>, V, LessThanOrEqual.Params<V>> =
    LessThanOrEqual(max).toValidationRule(errorMessage)

class GreaterThan<V>(private val min: V) :
    Check<Comparable<V>, GreaterThan.Params<V>, GreaterThan<V>>
{
    override val params = Params(min)

    override suspend fun invoke(value: Comparable<V>): Boolean =
        value > min

    data class Params<V>(val min: V) : Check.Params<GreaterThan<V>>()
}

fun <V : Comparable<V>> beGreaterThan(
    min: V,
    errorMessage: LazyErrorMessage<GreaterThan<V>, V, GreaterThan.Params<V>> =
        { "${validationPath()} must be greater than ${validationParams.min}" },
): ValidationRule<GreaterThan<V>, V, GreaterThan.Params<V>> =
    GreaterThan(min).toValidationRule(errorMessage)

class GreaterThanOrEqual<V>(private val min: V) :
    Check<Comparable<V>, GreaterThanOrEqual.Params<V>, GreaterThanOrEqual<V>>
{
    override val params = Params(min)

    override suspend fun invoke(value: Comparable<V>): Boolean =
        value >= min

    data class Params<V>(val min: V) : Check.Params<GreaterThanOrEqual<V>>()
}

fun <V : Comparable<V>> notBeLessThan(
    min: V,
    errorMessage: LazyErrorMessage<GreaterThanOrEqual<V>, V, GreaterThanOrEqual.Params<V>> =
        { "${validationPath()} must not be less than ${validationParams.min}" },
): ValidationRule<GreaterThanOrEqual<V>, V, GreaterThanOrEqual.Params<V>> =
    GreaterThanOrEqual(min).toValidationRule(errorMessage)
