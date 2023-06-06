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

class InRange<V : Comparable<V>>(range: ClosedRange<V>) :
    ParameterizedCheck<V, InRange.Params<V>> by (
            Params(range) and { it in range }
    )
{
    data class Params<V : Comparable<V>>(val range: ClosedRange<V>) :
        ParamsOf<InRange<V>, Params<V>>

    class RuleDescriptor<V : Comparable<V>> : ValidationRule.Descriptor<V, InRange<V>>(Check.key())
}

fun <T : Comparable<T>> ValidationRules<T>.inRange(
    range: ClosedRange<T>,
    errorMessage: LazyErrorMessage<InRange.RuleDescriptor<T>, T, InRange<T>> =
        { "Value must be in range ${context.params.range}" },
): ValidationRule<InRange.RuleDescriptor<T>, T, InRange<T>> =
    InRange(range).toValidationRule(InRange.RuleDescriptor(), errorMessage)

class OutsideRange<V : Comparable<V>>(range: ClosedRange<V>) :
    ParameterizedCheck<V, OutsideRange.Params<V>> by (
            Params(range) and { it !in range }
    )
{
    data class Params<V : Comparable<V>>(val range: ClosedRange<V>) :
        ParamsOf<OutsideRange<V>, Params<V>>

    class RuleDescriptor<V : Comparable<V>> : ValidationRule.Descriptor<V, OutsideRange<V>>(Check.key())
}

fun <T : Comparable<T>> ValidationRules<T>.outsideRange(
    range: ClosedRange<T>,
    errorMessage: LazyErrorMessage<OutsideRange.RuleDescriptor<T>, T, OutsideRange<T>> =
        { "Value must not be in range ${context.params.range}" },
): ValidationRule<OutsideRange.RuleDescriptor<T>, T, OutsideRange<T>> =
    OutsideRange(range).toValidationRule(OutsideRange.RuleDescriptor(), errorMessage)
