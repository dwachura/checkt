package io.dwsoft.checkt.basic.checks

import io.dwsoft.checkt.core.Check
import io.dwsoft.checkt.core.Check.Context
import io.dwsoft.checkt.core.ErrorDetailsBuilder
import io.dwsoft.checkt.core.ValidationRule
import io.dwsoft.checkt.core.errorMessage
import io.dwsoft.checkt.core.toDisplayed
import io.dwsoft.checkt.core.toValidationRule

class Equals<V>(private val other: V) : Check<V, Equals.Key, Equals.Params<V>> {
    override val context = Context.of(Key, Params(other))

    override fun invoke(value: V): Boolean =
        value == other

    object Key : Check.Key
    data class Params<V>(val other: V) : Check.Params()
}

fun <V> beEqualTo(
    other: V,
    errorDetailsBuilder: ErrorDetailsBuilder<V, Equals.Key, Equals.Params<V>> =
        errorMessage { "${validationPath()} must equal to ${validationParams.other}" },
): ValidationRule<V, Equals.Key, Equals.Params<V>> =
    Equals(other).toValidationRule(errorDetailsBuilder)

class IsDifferent<V>(private val other: V) : Check<V, IsDifferent.Key, IsDifferent.Params<V>> {
    override val context = Context.of(Key, Params(other))

    override fun invoke(value: V): Boolean =
        value != other

    object Key : Check.Key
    data class Params<V>(val other: V) : Check.Params()
}

fun <V> beDifferentThan(
    other: V,
    errorDetailsBuilder: ErrorDetailsBuilder<V, IsDifferent.Key, IsDifferent.Params<V>> =
        errorMessage { "${validationPath()} must be different than ${validationParams.other}" },
): ValidationRule<V, IsDifferent.Key, IsDifferent.Params<V>> =
    IsDifferent(other).toValidationRule(errorDetailsBuilder)
