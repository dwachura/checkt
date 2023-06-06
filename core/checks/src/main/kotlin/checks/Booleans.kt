package io.dwsoft.checkt.core.checks

import io.dwsoft.checkt.core.Check
import io.dwsoft.checkt.core.LazyErrorMessage
import io.dwsoft.checkt.core.ValidationRule
import io.dwsoft.checkt.core.ValidationRules
import io.dwsoft.checkt.core.key

object BeTrue : ValidationRule.Descriptor<Boolean?, Equals<Boolean?>>(Check.key())

fun ValidationRules<Boolean?>.beTrue(
    errorMessage: LazyErrorMessage<BeTrue, Boolean?, Equals<Boolean?>> = { "Value must be true" }
): ValidationRule<BeTrue, Boolean?, Equals<Boolean?>> =
    Equals(true as Boolean?).toValidationRule(BeTrue, errorMessage)

object BeFalse : ValidationRule.Descriptor<Boolean?, Equals<Boolean?>>(Check.key())

fun ValidationRules<Boolean?>.notBeTrue(
    errorMessage: LazyErrorMessage<NotBeTrue, Boolean?, IsDifferent<Boolean?>> = { "Value must not be true" }
): ValidationRule<NotBeTrue, Boolean?, IsDifferent<Boolean?>> =
    IsDifferent(true as Boolean?).toValidationRule(NotBeTrue, errorMessage)

object NotBeTrue : ValidationRule.Descriptor<Boolean?, IsDifferent<Boolean?>>(Check.key())

fun ValidationRules<Boolean?>.beFalse(
    errorMessage: LazyErrorMessage<BeFalse, Boolean?, Equals<Boolean?>> = { "Value must be false" }
): ValidationRule<BeFalse, Boolean?, Equals<Boolean?>> =
    Equals(false as Boolean?).toValidationRule(BeFalse, errorMessage)

object NotBeFalse : ValidationRule.Descriptor<Boolean?, IsDifferent<Boolean?>>(Check.key())

fun ValidationRules<Boolean?>.notBeFalse(
    errorMessage: LazyErrorMessage<NotBeFalse, Boolean?, IsDifferent<Boolean?>> = { "Value must not be false" }
): ValidationRule<NotBeFalse, Boolean?, IsDifferent<Boolean?>> =
    IsDifferent(false as Boolean?).toValidationRule(NotBeFalse, errorMessage)
