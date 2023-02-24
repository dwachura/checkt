package io.dwsoft.checkt.core.checks

import io.dwsoft.checkt.core.LazyErrorMessage
import io.dwsoft.checkt.core.ParameterizedCheck
import io.dwsoft.checkt.core.ParamsOf
import io.dwsoft.checkt.core.ValidationRule
import io.dwsoft.checkt.core.ValidationRules
import io.dwsoft.checkt.core.and
import io.dwsoft.checkt.core.params

class LessThan<V>(max: V) :
    ParameterizedCheck<Comparable<V>, LessThan.Params<V>> by (
            Params(max) and { it < max }
    )
{
    data class Params<V>(val max: V) : ParamsOf<LessThan<V>, Params<V>>
}

fun <T> ValidationRules<Comparable<T>>.lessThan(
    max: T,
    errorMessage: LazyErrorMessage<LessThan<T>, Comparable<T>> =
        { "Value must be less than ${context.params.max}" },
): ValidationRule<Comparable<T>, LessThan<T>> =
    LessThan(max).toValidationRule(errorMessage)

class LessThanOrEqual<V>(private val max: V) :
    ParameterizedCheck<Comparable<V>, LessThanOrEqual.Params<V>> by (
            Params(max) and { it <= max }
    )
{
    data class Params<V>(val max: V) : ParamsOf<LessThanOrEqual<V>, Params<V>>
}

fun <T> ValidationRules<Comparable<T>>.notGreaterThan(
    max: T,
    errorMessage: LazyErrorMessage<LessThanOrEqual<T>, Comparable<T>> =
        { "Value must not be greater than ${context.params.max}" },
): ValidationRule<Comparable<T>, LessThanOrEqual<T>> =
    LessThanOrEqual(max).toValidationRule(errorMessage)

class GreaterThan<V>(private val min: V) :
    ParameterizedCheck<Comparable<V>, GreaterThan.Params<V>> by (
            Params(min) and { it > min }
    )
{
    data class Params<V>(val min: V) : ParamsOf<GreaterThan<V>, Params<V>>
}

fun <T> ValidationRules<Comparable<T>>.greaterThan(
    min: T,
    errorMessage: LazyErrorMessage<GreaterThan<T>, Comparable<T>> =
        { "Value must be greater than ${context.params.min}" },
): ValidationRule<Comparable<T>, GreaterThan<T>> =
    GreaterThan(min).toValidationRule(errorMessage)

class GreaterThanOrEqual<V>(private val min: V) :
    ParameterizedCheck<Comparable<V>, GreaterThanOrEqual.Params<V>> by (
            Params(min) and { it >= min }
    )
{
    data class Params<V>(val min: V) : ParamsOf<GreaterThanOrEqual<V>, Params<V>>
}

fun <T> ValidationRules<Comparable<T>>.notLessThan(
    min: T,
    errorMessage: LazyErrorMessage<GreaterThanOrEqual<T>, Comparable<T>> =
        { "Value must not be less than ${context.params.min}" },
): ValidationRule<Comparable<T>, GreaterThanOrEqual<T>> =
    GreaterThanOrEqual(min).toValidationRule(errorMessage)
