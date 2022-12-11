package io.dwsoft.checkt.core.checks

import io.dwsoft.checkt.core.Check
import io.dwsoft.checkt.core.LazyErrorMessage
import io.dwsoft.checkt.core.ValidationRule
import io.dwsoft.checkt.core.toValidationRule

class InRange<V : Comparable<V>>(private val range: ClosedRange<V>) :
    Check<V, InRange.Params<V>, InRange<V>>
{
    override val params = Params(range)

    override suspend fun invoke(value: V): Boolean =
        value in range

    data class Params<V : Comparable<V>>(val range: ClosedRange<V>) : Check.Params<InRange<V>>()
}

fun <V : Comparable<V>> beInRange(
    range: ClosedRange<V>,
    errorMessage: LazyErrorMessage<InRange<V>, V, InRange.Params<V>> =
        { "${validationPath()} must be in range ${validationParams.range}" },
): ValidationRule<InRange<V>, V, InRange.Params<V>> =
    InRange(range).toValidationRule(errorMessage)

class OutsideRange<V : Comparable<V>>(private val range: ClosedRange<V>) :
    Check<V, OutsideRange.Params<V>, OutsideRange<V>>
{
    override val params = Params(range)

    override suspend fun invoke(value: V): Boolean =
        value !in range

    data class Params<V : Comparable<V>>(val range: ClosedRange<V>) : Check.Params<OutsideRange<V>>()
}

fun <V : Comparable<V>> beOutsideRange(
    range: ClosedRange<V>,
    errorMessage: LazyErrorMessage<OutsideRange<V>, V, OutsideRange.Params<V>> =
        { "${validationPath()} must not be in range ${validationParams.range}" },
): ValidationRule<OutsideRange<V>, V, OutsideRange.Params<V>> =
    OutsideRange(range).toValidationRule(errorMessage)
