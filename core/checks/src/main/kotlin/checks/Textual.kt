package io.dwsoft.checkt.core.checks

import io.dwsoft.checkt.core.Check
import io.dwsoft.checkt.core.LazyErrorMessage
import io.dwsoft.checkt.core.ValidationRule
import io.dwsoft.checkt.core.ValidationRules
import io.dwsoft.checkt.core.key

object NotBlank : Check<CharSequence> by Check({ it.isNotBlank() }) {
    object RuleDescriptor : ValidationRule.Descriptor<CharSequence, NotBlank>(Check.key())
}

fun ValidationRules<CharSequence>.notBlank(
    errorMessage: LazyErrorMessage<NotBlank.RuleDescriptor, CharSequence, NotBlank> =
        { "Value must not be blank" },
) : ValidationRule<NotBlank.RuleDescriptor, CharSequence, NotBlank> =
    NotBlank.toValidationRule(NotBlank.RuleDescriptor, errorMessage)
