package io.dwsoft.checkt.core.checks

import io.dwsoft.checkt.core.LazyErrorMessage
import io.dwsoft.checkt.core.ValidationRule
import io.dwsoft.checkt.core.ValidationRules

object BeTrue : ValidationRule.Descriptor<Boolean?, Equals<Boolean?>, BeTrue> {
    override val defaultMessage: LazyErrorMessage<BeTrue, Boolean?> = { "Value must be true" }
}

val ValidationRules<Boolean?>.beTrue: ValidationRule<BeTrue, Boolean?>
    get() = Equals(true as Boolean?).toValidationRule(BeTrue)

object BeFalse : ValidationRule.Descriptor<Boolean?, Equals<Boolean?>, BeFalse> {
    override val defaultMessage: LazyErrorMessage<BeFalse, Boolean?> = { "Value must be false" }
}

val ValidationRules<Boolean?>.notBeTrue: ValidationRule<NotBeTrue, Boolean?>
    get() = IsDifferent(true as Boolean?).toValidationRule(NotBeTrue)

object NotBeTrue : ValidationRule.Descriptor<Boolean?, IsDifferent<Boolean?>, NotBeTrue> {
    override val defaultMessage: LazyErrorMessage<NotBeTrue, Boolean?> = { "Value must not be true" }
}

val ValidationRules<Boolean?>.beFalse: ValidationRule<BeFalse, Boolean?>
    get() = Equals(false as Boolean?).toValidationRule(BeFalse)

object NotBeFalse : ValidationRule.Descriptor<Boolean?, IsDifferent<Boolean?>, NotBeFalse> {
    override val defaultMessage: LazyErrorMessage<NotBeFalse, Boolean?> = { "Value must not be false" }
}

val ValidationRules<Boolean?>.notBeFalse: ValidationRule<NotBeFalse, Boolean?>
    get() = IsDifferent(false as Boolean?).toValidationRule(NotBeFalse)
