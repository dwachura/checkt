package io.dwsoft.checkt.core.checks

import io.dwsoft.checkt.core.LazyErrorMessage
import io.dwsoft.checkt.core.ParameterizedCheck
import io.dwsoft.checkt.core.ParamsOf
import io.dwsoft.checkt.core.ValidationRule
import io.dwsoft.checkt.core.ValidationRules
import io.dwsoft.checkt.core.and
import io.dwsoft.checkt.core.params

class Equals<V>(other: V) :
    ParameterizedCheck<V, Equals.Params<V>> by (
            Params(other) and { it == other }
    )
{
    data class Params<V>(val other: V) : ParamsOf<Equals<V>, Params<V>>

    class Rule<V> : ValidationRule.Descriptor<V, Equals<V>, Rule<V>> {
        override val defaultMessage: LazyErrorMessage<Rule<V>, V> =
            { "Value must equal to ${context.params.other}" }
    }
}

fun <T> ValidationRules<T>.equalTo(other: T): ValidationRule<Equals.Rule<T>, T> =
    Equals(other).toValidationRule(Equals.Rule())

class IsDifferent<V>(other: V) :
    ParameterizedCheck<V, IsDifferent.Params<V>> by (
            Params(other) and { it != other }
    )
{
    data class Params<V>(val other: V) : ParamsOf<IsDifferent<V>, Params<V>>

    class Rule<V> : ValidationRule.Descriptor<V, IsDifferent<V>, Rule<V>> {
        override val defaultMessage: LazyErrorMessage<Rule<V>, V> =
            { "Value must be different than ${context.params.other}" }
    }
}

fun <T> ValidationRules<T>.differentThan(other: T): ValidationRule<IsDifferent.Rule<T>, T> =
    IsDifferent(other).toValidationRule(IsDifferent.Rule())
