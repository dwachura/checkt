package io.dwsoft.checkt.core.checks

import io.dwsoft.checkt.core.LazyErrorMessage
import io.dwsoft.checkt.core.ParameterizedCheck
import io.dwsoft.checkt.core.ParamsOf
import io.dwsoft.checkt.core.ValidationRule
import io.dwsoft.checkt.core.ValidationRules
import io.dwsoft.checkt.core.params

class LessThan<V>(private val max: V) :
    ParameterizedCheck<Comparable<V>, LessThan.Params<V>>
{
    override val params = Params(max)

    override suspend fun invoke(value: Comparable<V>): Boolean =
        value < max

    data class Params<V>(val max: V) : ParamsOf<LessThan<V>, Params<V>>
}

fun <T> ValidationRules<Comparable<T>>.lessThan(
    max: T,
    errorMessage: LazyErrorMessage<LessThan<T>, Comparable<T>> =
        { "Value must be less than ${context.params.max}" },
): ValidationRule<LessThan<T>, Comparable<T>> =
    LessThan(max).toValidationRule(errorMessage)

class LessThanOrEqual<V>(private val max: V) :
    ParameterizedCheck<Comparable<V>, LessThanOrEqual.Params<V>>
{
    override val params = Params(max)

    override suspend fun invoke(value: Comparable<V>): Boolean =
        value <= max

    data class Params<V>(val max: V) : ParamsOf<LessThanOrEqual<V>, Params<V>>
}

fun <T> ValidationRules<Comparable<T>>.notGreaterThan(
    max: T,
    errorMessage: LazyErrorMessage<LessThanOrEqual<T>, Comparable<T>> =
        { "Value must not be greater than ${context.params.max}" },
): ValidationRule<LessThanOrEqual<T>, Comparable<T>> =
    LessThanOrEqual(max).toValidationRule(errorMessage)

class GreaterThan<V>(private val min: V) :
    ParameterizedCheck<Comparable<V>, GreaterThan.Params<V>>
{
    override val params = Params(min)

    override suspend fun invoke(value: Comparable<V>): Boolean =
        value > min

    data class Params<V>(val min: V) : ParamsOf<GreaterThan<V>, Params<V>>
}

fun <T> ValidationRules<Comparable<T>>.greaterThan(
    min: T,
    errorMessage: LazyErrorMessage<GreaterThan<T>, Comparable<T>> =
        { "Value must be greater than ${context.params.min}" },
): ValidationRule<GreaterThan<T>, Comparable<T>> =
    GreaterThan(min).toValidationRule(errorMessage)

class GreaterThanOrEqual<V>(private val min: V) :
    ParameterizedCheck<Comparable<V>, GreaterThanOrEqual.Params<V>>
{
    override val params = Params(min)

    override suspend fun invoke(value: Comparable<V>): Boolean =
        value >= min

    data class Params<V>(val min: V) : ParamsOf<GreaterThanOrEqual<V>, Params<V>>
}

fun <T> ValidationRules<Comparable<T>>.notLessThan(
    min: T,
    errorMessage: LazyErrorMessage<GreaterThanOrEqual<T>, Comparable<T>> =
        { "Value must not be less than ${context.params.min}" },
): ValidationRule<GreaterThanOrEqual<T>, Comparable<T>> =
    GreaterThanOrEqual(min).toValidationRule(errorMessage)
