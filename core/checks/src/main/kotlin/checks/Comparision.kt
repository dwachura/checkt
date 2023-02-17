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

fun <T> ValidationRules<Comparable<T>>.lessThan(
    max: T,
    errorMessage: LazyErrorMessage<LessThan<T>, Comparable<T>, LessThan.Params<T>> =
        { "Value must be less than ${validationParams.max}" },
): ValidationRule<LessThan<T>, Comparable<T>, LessThan.Params<T>> =
    LessThan(max).toValidationRule(errorMessage)

class LessThanOrEqual<V>(private val max: V) :
    Check<Comparable<V>, LessThanOrEqual.Params<V>, LessThanOrEqual<V>>
{
    override val params = Params(max)

    override suspend fun invoke(value: Comparable<V>): Boolean =
        value <= max

    data class Params<V>(val max: V) : Check.Params<LessThanOrEqual<V>>()
}

fun <T> ValidationRules<Comparable<T>>.notGreaterThan(
    max: T,
    errorMessage: LazyErrorMessage<LessThanOrEqual<T>, Comparable<T>, LessThanOrEqual.Params<T>> =
        { "Value must not be greater than ${validationParams.max}" },
): ValidationRule<LessThanOrEqual<T>, Comparable<T>, LessThanOrEqual.Params<T>> =
    LessThanOrEqual(max).toValidationRule(errorMessage)

class GreaterThan<V>(private val min: V) :
    Check<Comparable<V>, GreaterThan.Params<V>, GreaterThan<V>>
{
    override val params = Params(min)

    override suspend fun invoke(value: Comparable<V>): Boolean =
        value > min

    data class Params<V>(val min: V) : Check.Params<GreaterThan<V>>()
}

fun <T> ValidationRules<Comparable<T>>.greaterThan(
    min: T,
    errorMessage: LazyErrorMessage<GreaterThan<T>, Comparable<T>, GreaterThan.Params<T>> =
        { "Value must be greater than ${validationParams.min}" },
): ValidationRule<GreaterThan<T>, Comparable<T>, GreaterThan.Params<T>> =
    GreaterThan(min).toValidationRule(errorMessage)

class GreaterThanOrEqual<V>(private val min: V) :
    Check<Comparable<V>, GreaterThanOrEqual.Params<V>, GreaterThanOrEqual<V>>
{
    override val params = Params(min)

    override suspend fun invoke(value: Comparable<V>): Boolean =
        value >= min

    data class Params<V>(val min: V) : Check.Params<GreaterThanOrEqual<V>>()
}

fun <T> ValidationRules<Comparable<T>>.notLessThan(
    min: T,
    errorMessage: LazyErrorMessage<GreaterThanOrEqual<T>, Comparable<T>, GreaterThanOrEqual.Params<T>> =
        { "Value must not be less than ${validationParams.min}" },
): ValidationRule<GreaterThanOrEqual<T>, Comparable<T>, GreaterThanOrEqual.Params<T>> =
    GreaterThanOrEqual(min).toValidationRule(errorMessage)
