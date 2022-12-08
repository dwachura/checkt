package io.dwsoft.checkt.core.checks

import io.dwsoft.checkt.core.Check
import io.dwsoft.checkt.core.LazyErrorDetails
import io.dwsoft.checkt.core.ValidationRule
import io.dwsoft.checkt.core.toValidationRule

class Equals<V>(private val other: V) : Check<V, Equals.Params<V>, Equals<V>> {
    override val params = Params(other)

    override fun invoke(value: V): Boolean =
        value == other

    data class Params<V>(val other: V) : Check.Params<Equals<V>>()
}

fun <V> beEqualTo(
    other: V,
    errorDetails: LazyErrorDetails<Equals<V>, V, Equals.Params<V>> =
        { "${validationPath()} must equal to ${validationParams.other}" },
): ValidationRule<Equals<V>, V, Equals.Params<V>> =
    Equals(other).toValidationRule(errorDetails)

class IsDifferent<V>(private val other: V) : Check<V, IsDifferent.Params<V>, IsDifferent<V>> {
    override val params = Params(other)

    override fun invoke(value: V): Boolean =
        value != other

    data class Params<V>(val other: V) : Check.Params<IsDifferent<V>>()
}

fun <V> beDifferentThan(
    other: V,
    errorDetails: LazyErrorDetails<IsDifferent<V>, V, IsDifferent.Params<V>> =
        { "${validationPath()} must be different than ${validationParams.other}" },
): ValidationRule<IsDifferent<V>, V, IsDifferent.Params<V>> =
    IsDifferent(other).toValidationRule(errorDetails)
