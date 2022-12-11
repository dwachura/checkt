package io.dwsoft.checkt.core.checks

import io.dwsoft.checkt.core.Check
import io.dwsoft.checkt.core.LazyErrorMessage
import io.dwsoft.checkt.core.ValidationRule
import io.dwsoft.checkt.core.ValidationRules

class Equals<V>(private val other: V) : Check<V, Equals.Params<V>, Equals<V>> {
    override val params = Params(other)

    override suspend fun invoke(value: V): Boolean =
        value == other

    data class Params<V>(val other: V) : Check.Params<Equals<V>>()
}

fun <T> ValidationRules<T>.beEqualTo(
    other: T,
    errorMessage: LazyErrorMessage<Equals<T>, T, Equals.Params<T>> =
        { "${validationPath()} must equal to ${validationParams.other}" },
): ValidationRule<Equals<T>, T, Equals.Params<T>> =
    Equals(other).toValidationRule(errorMessage)

class IsDifferent<V>(private val other: V) : Check<V, IsDifferent.Params<V>, IsDifferent<V>> {
    override val params = Params(other)

    override suspend fun invoke(value: V): Boolean =
        value != other

    data class Params<V>(val other: V) : Check.Params<IsDifferent<V>>()
}

fun <T> ValidationRules<T>.beDifferentThan(
    other: T,
    errorMessage: LazyErrorMessage<IsDifferent<T>, T, IsDifferent.Params<T>> =
        { "${validationPath()} must be different than ${validationParams.other}" },
): ValidationRule<IsDifferent<T>, T, IsDifferent.Params<T>> =
    IsDifferent(other).toValidationRule(errorMessage)
