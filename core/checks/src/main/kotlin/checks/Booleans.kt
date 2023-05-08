@file:Suppress("UNCHECKED_CAST")

package io.dwsoft.checkt.core.checks

import io.dwsoft.checkt.core.LazyErrorMessage
import io.dwsoft.checkt.core.ValidationRule
import io.dwsoft.checkt.core.ValidationRules

fun ValidationRules<Boolean?>.beTrue(
    errorMessage: LazyErrorMessage<Equals<Boolean?>, Boolean?> =
        { "Value must be true" }
): ValidationRule<Boolean?, Equals<Boolean?>> =
    (Equals(true) as Equals<Boolean?>).toValidationRule(errorMessage)

fun ValidationRules<Boolean?>.notBeTrue(
    errorMessage: LazyErrorMessage<IsDifferent<Boolean?>, Boolean?> =
        { "Value must not be true" }
): ValidationRule<Boolean?, IsDifferent<Boolean?>> =
    (IsDifferent(true) as IsDifferent<Boolean?>).toValidationRule(errorMessage)

fun ValidationRules<Boolean?>.beFalse(
    errorMessage: LazyErrorMessage<Equals<Boolean?>, Boolean?> =
        { "Value must be false" }
): ValidationRule<Boolean?, Equals<Boolean?>> =
    (Equals(false) as Equals<Boolean?>).toValidationRule(errorMessage)

fun ValidationRules<Boolean?>.notBeFalse(
    errorMessage: LazyErrorMessage<IsDifferent<Boolean?>, Boolean?> =
        { "Value must not be false" }
): ValidationRule<Boolean?, IsDifferent<Boolean?>> =
    (IsDifferent(false) as IsDifferent<Boolean?>).toValidationRule(errorMessage)
