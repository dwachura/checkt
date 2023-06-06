package io.dwsoft.checkt.core.checks

import io.dwsoft.checkt.core.Check
import io.dwsoft.checkt.core.LazyErrorMessage
import io.dwsoft.checkt.core.ParameterizedCheck
import io.dwsoft.checkt.core.ParamsOf
import io.dwsoft.checkt.core.ValidationRule
import io.dwsoft.checkt.core.ValidationRules
import io.dwsoft.checkt.core.and
import io.dwsoft.checkt.core.key
import io.dwsoft.checkt.core.params

class LessThan<V>(max: V) :
    ParameterizedCheck<Comparable<V>, LessThan.Params<V>> by (
            Params(max) and { it < max }
    )
{
    data class Params<V>(val max: V) : ParamsOf<LessThan<V>, Params<V>>

    class RuleDescriptor<V> : ValidationRule.Descriptor<Comparable<V>, LessThan<V>>(Check.key())
}

fun <T> ValidationRules<Comparable<T>>.lessThan(
    max: T,
    errorMessage: LazyErrorMessage<LessThan.RuleDescriptor<T>, Comparable<T>, LessThan<T>> =
        { "Value must be less than ${context.params.max}" },
): ValidationRule<LessThan.RuleDescriptor<T>, Comparable<T>, LessThan<T>> =
    LessThan(max).toValidationRule(LessThan.RuleDescriptor(), errorMessage)

class LessThanOrEqual<V>(private val max: V) :
    ParameterizedCheck<Comparable<V>, LessThanOrEqual.Params<V>> by (
            Params(max) and { it <= max }
    )
{
    data class Params<V>(val max: V) : ParamsOf<LessThanOrEqual<V>, Params<V>>

    class RuleDescriptor<V> : ValidationRule.Descriptor<Comparable<V>, LessThanOrEqual<V>>(Check.key())
}

fun <T> ValidationRules<Comparable<T>>.notGreaterThan(
    max: T,
    errorMessage: LazyErrorMessage<LessThanOrEqual.RuleDescriptor<T>, Comparable<T>, LessThanOrEqual<T>> =
        { "Value must not be greater than ${context.params.max}" },
): ValidationRule<LessThanOrEqual.RuleDescriptor<T>, Comparable<T>, LessThanOrEqual<T>> =
    LessThanOrEqual(max).toValidationRule(LessThanOrEqual.RuleDescriptor(), errorMessage)

class GreaterThan<V>(private val min: V) :
    ParameterizedCheck<Comparable<V>, GreaterThan.Params<V>> by (
            Params(min) and { it > min }
    )
{
    data class Params<V>(val min: V) : ParamsOf<GreaterThan<V>, Params<V>>

    class RuleDescriptor<V> : ValidationRule.Descriptor<Comparable<V>, GreaterThan<V>>(Check.key())
}

fun <T> ValidationRules<Comparable<T>>.greaterThan(
    min: T,
    errorMessage: LazyErrorMessage<GreaterThan.RuleDescriptor<T>, Comparable<T>, GreaterThan<T>> =
        { "Value must be greater than ${context.params.min}" },
): ValidationRule<GreaterThan.RuleDescriptor<T>, Comparable<T>, GreaterThan<T>> =
    GreaterThan(min).toValidationRule(GreaterThan.RuleDescriptor(), errorMessage)

class GreaterThanOrEqual<V>(private val min: V) :
    ParameterizedCheck<Comparable<V>, GreaterThanOrEqual.Params<V>> by (
            Params(min) and { it >= min }
    )
{
    data class Params<V>(val min: V) : ParamsOf<GreaterThanOrEqual<V>, Params<V>>

    class RuleDescriptor<V> : ValidationRule.Descriptor<Comparable<V>, GreaterThanOrEqual<V>>(Check.key())
}

fun <T> ValidationRules<Comparable<T>>.notLessThan(
    min: T,
    errorMessage: LazyErrorMessage<GreaterThanOrEqual.RuleDescriptor<T>, Comparable<T>, GreaterThanOrEqual<T>> =
        { "Value must not be less than ${context.params.min}" },
): ValidationRule<GreaterThanOrEqual.RuleDescriptor<T>, Comparable<T>, GreaterThanOrEqual<T>> =
    GreaterThanOrEqual(min).toValidationRule(GreaterThanOrEqual.RuleDescriptor(), errorMessage)
