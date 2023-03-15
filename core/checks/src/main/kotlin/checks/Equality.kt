package io.dwsoft.checkt.core.checks

import io.dwsoft.checkt.core.LazyErrorMessage
import io.dwsoft.checkt.core.ParameterizedCheck
import io.dwsoft.checkt.core.ParamsOf
import io.dwsoft.checkt.core.ValidationRule
import io.dwsoft.checkt.core.ValidationRules
import io.dwsoft.checkt.core.and
import io.dwsoft.checkt.core.params

class Equals<V>(other: V) : ParameterizedCheck<V, Equals.Params<V>> by (
        Params(other) and { it == other }
) {
    data class Params<V>(val other: V) : ParamsOf<Equals<V>, Params<V>>
}

fun <T> ValidationRules<T>.equalTo(
    other: T,
    errorMessage: LazyErrorMessage<Equals<T>, T> =
        { "Value must equal to ${context.params.other}" },
): ValidationRule<T, Equals<T>> =
    Equals(other).toValidationRule(errorMessage)

class IsDifferent<V>(other: V) : ParameterizedCheck<V, IsDifferent.Params<V>> by (
        Params(other) and { it != other }
) {
    data class Params<V>(val other: V) : ParamsOf<IsDifferent<V>, Params<V>>
}

fun <T> ValidationRules<T>.differentThan(
    other: T,
    errorMessage: LazyErrorMessage<IsDifferent<T>, T> =
        { "Value must be different than ${context.params.other}" },
): ValidationRule<T, IsDifferent<T>> =
    IsDifferent(other).toValidationRule(errorMessage)

// TODO: extract to other module (additional rules) + test???

fun ValidationRules<Boolean>.beTrue(
    errorMessage: LazyErrorMessage<Equals<Boolean>, Boolean> =
        { "Value must be true" }
): ValidationRule<Boolean, Equals<Boolean>> =
    Equals(true).toValidationRule(errorMessage)

fun ValidationRules<Boolean?>.notBeTrue(
    errorMessage: LazyErrorMessage<IsDifferent<Boolean?>, Boolean?> =
        { "Value must not be true" }
): ValidationRule<Boolean?, IsDifferent<Boolean?>> =
    @Suppress("UNCHECKED_CAST")
    (IsDifferent(true) as IsDifferent<Boolean?>).toValidationRule(errorMessage)

fun ValidationRules<Boolean>.beFalse(
    errorMessage: LazyErrorMessage<Equals<Boolean>, Boolean> =
        { "Value must be false" }
): ValidationRule<Boolean, Equals<Boolean>> =
    Equals(false).toValidationRule(errorMessage)

fun ValidationRules<Boolean?>.notBeFalse(
    errorMessage: LazyErrorMessage<IsDifferent<Boolean?>, Boolean?> =
        { "Value must not be false" }
): ValidationRule<Boolean?, IsDifferent<Boolean?>> =
    @Suppress("UNCHECKED_CAST")
    (IsDifferent(false) as IsDifferent<Boolean?>).toValidationRule(errorMessage)
