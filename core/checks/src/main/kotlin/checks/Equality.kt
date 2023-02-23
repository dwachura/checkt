package io.dwsoft.checkt.core.checks

import io.dwsoft.checkt.core.LazyErrorMessage
import io.dwsoft.checkt.core.ParameterizedCheck
import io.dwsoft.checkt.core.ParamsOf
import io.dwsoft.checkt.core.ValidationRule
import io.dwsoft.checkt.core.ValidationRules
import io.dwsoft.checkt.core.params

class Equals<V>(private val other: V) :
    ParameterizedCheck<V, Equals.Params<V>>
{
    override val params = Params(other)

    override suspend fun invoke(value: V): Boolean =
        value == other

    data class Params<V>(val other: V) : ParamsOf<Equals<V>, Params<V>>
}

fun <T> ValidationRules<T>.equalTo(
    other: T,
    errorMessage: LazyErrorMessage<Equals<T>, T> =
        { "Value must equal to ${context.params.other}" },
): ValidationRule<Equals<T>, T> =
    Equals(other).toValidationRule(errorMessage)

class IsDifferent<V>(private val other: V) :
    ParameterizedCheck<V, IsDifferent.Params<V>>
{
    override val params = Params(other)

    override suspend fun invoke(value: V): Boolean =
        value != other

    data class Params<V>(val other: V) : ParamsOf<IsDifferent<V>, Params<V>>
}

fun <T> ValidationRules<T>.differentThan(
    other: T,
    errorMessage: LazyErrorMessage<IsDifferent<T>, T> =
        { "Value must be different than ${context.params.other}" },
): ValidationRule<IsDifferent<T>, T> =
    IsDifferent(other).toValidationRule(errorMessage)
