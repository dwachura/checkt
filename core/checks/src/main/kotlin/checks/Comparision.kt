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

    class Rule<V> : ValidationRule.Descriptor<Comparable<V>, LessThan<V>, Rule<V>> {
        override val defaultMessage: LazyErrorMessage<Rule<V>, Comparable<V>> =
            { "Value must be less than ${context.params.max}" }
    }
}

fun <T> ValidationRules<Comparable<T>>.lessThan(max: T):
        ValidationRule<LessThan.Rule<T>, Comparable<T>> =
    LessThan(max).toValidationRule(LessThan.Rule())

class LessThanOrEqual<V>(private val max: V) :
    ParameterizedCheck<Comparable<V>, LessThanOrEqual.Params<V>> by (
            Params(max) and { it <= max }
    )
{
    data class Params<V>(val max: V) : ParamsOf<LessThanOrEqual<V>, Params<V>>

    class Rule<V> : ValidationRule.Descriptor<Comparable<V>, LessThanOrEqual<V>, Rule<V>> {
        override val defaultMessage: LazyErrorMessage<Rule<V>, Comparable<V>> =
            { "Value must not be greater than ${context.params.max}" }
    }
}

fun <T> ValidationRules<Comparable<T>>.notGreaterThan(max: T):
        ValidationRule<LessThanOrEqual.Rule<T>, Comparable<T>> =
    LessThanOrEqual(max).toValidationRule(LessThanOrEqual.Rule())

class GreaterThan<V>(private val min: V) :
    ParameterizedCheck<Comparable<V>, GreaterThan.Params<V>> by (
            Params(min) and { it > min }
    )
{
    data class Params<V>(val min: V) : ParamsOf<GreaterThan<V>, Params<V>>

    class Rule<V> : ValidationRule.Descriptor<Comparable<V>, GreaterThan<V>, Rule<V>> {
        override val defaultMessage: LazyErrorMessage<Rule<V>, Comparable<V>> =
            { "Value must be greater than ${context.params.min}" }
    }
}

fun <T> ValidationRules<Comparable<T>>.greaterThan(min: T):
        ValidationRule<GreaterThan.Rule<T>, Comparable<T>> =
    GreaterThan(min).toValidationRule(GreaterThan.Rule())

class GreaterThanOrEqual<V>(private val min: V) :
    ParameterizedCheck<Comparable<V>, GreaterThanOrEqual.Params<V>> by (
            Params(min) and { it >= min }
    )
{
    data class Params<V>(val min: V) : ParamsOf<GreaterThanOrEqual<V>, Params<V>>

    class Rule<V> : ValidationRule.Descriptor<Comparable<V>, GreaterThanOrEqual<V>, Rule<V>> {
        override val defaultMessage: LazyErrorMessage<Rule<V>, Comparable<V>> =
            { "Value must not be less than ${context.params.min}" }
    }
}

fun <T> ValidationRules<Comparable<T>>.notLessThan(min: T):
        ValidationRule<GreaterThanOrEqual.Rule<T>, Comparable<T>> =
    GreaterThanOrEqual(min).toValidationRule(GreaterThanOrEqual.Rule())
