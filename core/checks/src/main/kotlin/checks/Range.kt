package io.dwsoft.checkt.core.checks

import io.dwsoft.checkt.core.LazyErrorMessage
import io.dwsoft.checkt.core.ParameterizedCheck
import io.dwsoft.checkt.core.ParamsOf
import io.dwsoft.checkt.core.ValidationRule
import io.dwsoft.checkt.core.ValidationRules
import io.dwsoft.checkt.core.and
import io.dwsoft.checkt.core.params

class InRange<V : Comparable<V>>(range: ClosedRange<V>) :
    ParameterizedCheck<V, InRange.Params<V>> by (
            Params(range) and { it in range }
    )
{
    data class Params<V : Comparable<V>>(val range: ClosedRange<V>) :
        ParamsOf<InRange<V>, Params<V>>

    class Rule<V : Comparable<V>> : ValidationRule.Descriptor<V, InRange<V>, Rule<V>> {
        override val defaultMessage: LazyErrorMessage<Rule<V>, V> =
            { "Value must be in range ${context.params.range}" }
    }
}

fun <T : Comparable<T>> ValidationRules<T>.inRange(
    range: ClosedRange<T>,
): ValidationRule<InRange.Rule<T>, T> =
    InRange(range).toValidationRule(InRange.Rule())

class OutsideRange<V : Comparable<V>>(range: ClosedRange<V>) :
    ParameterizedCheck<V, OutsideRange.Params<V>> by (
            Params(range) and { it !in range }
    )
{
    data class Params<V : Comparable<V>>(val range: ClosedRange<V>) :
        ParamsOf<OutsideRange<V>, Params<V>>

    class Rule<V : Comparable<V>> : ValidationRule.Descriptor<V, OutsideRange<V>, Rule<V>> {
        override val defaultMessage: LazyErrorMessage<Rule<V>, V> =
            { "Value must not be in range ${context.params.range}" }
    }
}

fun <T : Comparable<T>> ValidationRules<T>.outsideRange(
    range: ClosedRange<T>,
): ValidationRule<OutsideRange.Rule<T>, T> =
    OutsideRange(range).toValidationRule(OutsideRange.Rule())
