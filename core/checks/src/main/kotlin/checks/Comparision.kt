package io.dwsoft.checkt.core.checks

import io.dwsoft.checkt.core.Check
import io.dwsoft.checkt.core.LazyErrorMessage
import io.dwsoft.checkt.core.ValidationRule
import io.dwsoft.checkt.core.ValidationRules

class LessThan<V>(private val max: V) :
    Check<Comparable<V>, LessThan.Params<V>, LessThan<V>>
{
    override val params = Params(max)

    override suspend fun invoke(value: Comparable<V>): Boolean =
        value < max

    data class Params<V>(val max: V) : Check.Params<LessThan<V>>()
}

fun <T : Comparable<T>> ValidationRules<T>.beLessThan(
    max: T,
    errorMessage: LazyErrorMessage<LessThan<T>, T, LessThan.Params<T>> =
        { "${validationPath()} must be less than ${validationParams.max}" },
): ValidationRule<LessThan<T>, T, LessThan.Params<T>> =
    LessThan(max).toValidationRule(errorMessage)

class LessThanOrEqual<V>(private val max: V) :
    Check<Comparable<V>, LessThanOrEqual.Params<V>, LessThanOrEqual<V>>
{
    override val params = Params(max)

    override suspend fun invoke(value: Comparable<V>): Boolean =
        value <= max

    data class Params<V>(val max: V) : Check.Params<LessThanOrEqual<V>>()
}

fun <T : Comparable<T>> ValidationRules<T>.notBeGreaterThan(
    max: T,
    errorMessage: LazyErrorMessage<LessThanOrEqual<T>, T, LessThanOrEqual.Params<T>> =
        { "${validationPath()} must not be greater than ${validationParams.max}" },
): ValidationRule<LessThanOrEqual<T>, T, LessThanOrEqual.Params<T>> =
    LessThanOrEqual(max).toValidationRule(errorMessage)

class GreaterThan<V>(private val min: V) :
    Check<Comparable<V>, GreaterThan.Params<V>, GreaterThan<V>>
{
    override val params = Params(min)

    override suspend fun invoke(value: Comparable<V>): Boolean =
        value > min

    data class Params<V>(val min: V) : Check.Params<GreaterThan<V>>()
}

fun <T : Comparable<T>> ValidationRules<T>.beGreaterThan(
    min: T,
    errorMessage: LazyErrorMessage<GreaterThan<T>, T, GreaterThan.Params<T>> =
        { "${validationPath()} must be greater than ${validationParams.min}" },
): ValidationRule<GreaterThan<T>, T, GreaterThan.Params<T>> =
    GreaterThan(min).toValidationRule(errorMessage)

class GreaterThanOrEqual<V>(private val min: V) :
    Check<Comparable<V>, GreaterThanOrEqual.Params<V>, GreaterThanOrEqual<V>>
{
    override val params = Params(min)

    override suspend fun invoke(value: Comparable<V>): Boolean =
        value >= min

    data class Params<V>(val min: V) : Check.Params<GreaterThanOrEqual<V>>()
}

fun <T : Comparable<T>> ValidationRules<T>.notBeLessThan(
    min: T,
    errorMessage: LazyErrorMessage<GreaterThanOrEqual<T>, T, GreaterThanOrEqual.Params<T>> =
        { "${validationPath()} must not be less than ${validationParams.min}" },
): ValidationRule<GreaterThanOrEqual<T>, T, GreaterThanOrEqual.Params<T>> =
    GreaterThanOrEqual(min).toValidationRule(errorMessage)
