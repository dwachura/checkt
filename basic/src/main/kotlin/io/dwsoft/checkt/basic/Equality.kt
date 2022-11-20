package io.dwsoft.checkt.basic

import io.dwsoft.checkt.core.Check
import io.dwsoft.checkt.core.ErrorDetailsBuilder
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
    errorDetailsBuilder: ErrorDetailsBuilder<Equals<V>, V, Equals.Params<V>> =
        { "${validationPath()} must equal to ${validationParams.other}" },
): ValidationRule<Equals<V>, V, Equals.Params<V>> =
    Equals(other).toValidationRule(errorDetailsBuilder)

class IsDifferent<V>(private val other: V) : Check<V, IsDifferent.Params<V>, IsDifferent<V>> {
    override val params = Params(other)

    override fun invoke(value: V): Boolean =
        value != other

    data class Params<V>(val other: V) : Check.Params<IsDifferent<V>>()
}

fun <V> beDifferentThan(
    other: V,
    errorDetailsBuilder: ErrorDetailsBuilder<IsDifferent<V>, V, IsDifferent.Params<V>> =
        { "${validationPath()} must be different than ${validationParams.other}" },
): ValidationRule<IsDifferent<V>, V, IsDifferent.Params<V>> =
    IsDifferent(other).toValidationRule(errorDetailsBuilder)
