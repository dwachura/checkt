package io.dwsoft.checkt.core.checks

import io.dwsoft.checkt.core.Check
import io.dwsoft.checkt.core.LazyErrorMessage
import io.dwsoft.checkt.core.ValidationRule
import io.dwsoft.checkt.core.ValidationRules

class InRange<V : Comparable<V>>(private val range: ClosedRange<V>) :
    Check<V, InRange.Params<V>, InRange<V>>
{
    override val params = Params(range)

    override suspend fun invoke(value: V): Boolean =
        value in range

    data class Params<V : Comparable<V>>(val range: ClosedRange<V>) : Check.Params<InRange<V>>()
}

fun <T : Comparable<T>> ValidationRules<T>.inRange(
    range: ClosedRange<T>,
    errorMessage: LazyErrorMessage<InRange<T>, T, InRange.Params<T>> =
        { "Value must be in range ${validationParams.range}" },
): ValidationRule<InRange<T>, T, InRange.Params<T>> =
    InRange(range).toValidationRule(errorMessage)

class OutsideRange<V : Comparable<V>>(private val range: ClosedRange<V>) :
    Check<V, OutsideRange.Params<V>, OutsideRange<V>>
{
    override val params = Params(range)

    override suspend fun invoke(value: V): Boolean =
        value !in range

    data class Params<V : Comparable<V>>(val range: ClosedRange<V>) : Check.Params<OutsideRange<V>>()
}

fun <T : Comparable<T>> ValidationRules<T>.outsideRange(
    range: ClosedRange<T>,
    errorMessage: LazyErrorMessage<OutsideRange<T>, T, OutsideRange.Params<T>> =
        { "Value must not be in range ${validationParams.range}" },
): ValidationRule<OutsideRange<T>, T, OutsideRange.Params<T>> =
    OutsideRange(range).toValidationRule(errorMessage)
