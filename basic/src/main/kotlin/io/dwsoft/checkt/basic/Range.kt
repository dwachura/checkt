package io.dwsoft.checkt.basic

import io.dwsoft.checkt.core.Check
import io.dwsoft.checkt.core.Check.Context
import io.dwsoft.checkt.core.ErrorDetailsBuilder
import io.dwsoft.checkt.core.ValidationRule
import io.dwsoft.checkt.core.toValidationRule

class InRange<V : Comparable<V>>(private val range: ClosedRange<V>) :
    Check<V, InRange.Key, InRange.Params<V>>
{
    override val context = Context.of(Key, Params(range))

    override fun invoke(value: V): Boolean =
        value in range

    object Key : Check.Key
    data class Params<V : Comparable<V>>(val range: ClosedRange<V>) : Check.Params()
}

fun <V : Comparable<V>> beInRange(
    range: ClosedRange<V>,
    errorDetailsBuilder: ErrorDetailsBuilder<V, InRange.Key, InRange.Params<V>> =
        { "${validationPath()} must be in range ${validationParams.range}" },
): ValidationRule<V, InRange.Key, InRange.Params<V>> =
    InRange(range).toValidationRule(errorDetailsBuilder)

class OutsideRange<V : Comparable<V>>(private val range: ClosedRange<V>) :
    Check<V, OutsideRange.Key, OutsideRange.Params<V>>
{
    override val context = Context.of(Key, Params(range))

    override fun invoke(value: V): Boolean =
        value !in range

    object Key : Check.Key
    data class Params<V : Comparable<V>>(val range: ClosedRange<V>) : Check.Params()
}

fun <V : Comparable<V>> beOutsideRange(
    range: ClosedRange<V>,
    errorDetailsBuilder: ErrorDetailsBuilder<V, OutsideRange.Key, OutsideRange.Params<V>> =
        { "${validationPath()} must not be in range ${validationParams.range}" },
): ValidationRule<V, OutsideRange.Key, OutsideRange.Params<V>> =
    OutsideRange(range).toValidationRule(errorDetailsBuilder)
