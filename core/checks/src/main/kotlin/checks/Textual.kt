package io.dwsoft.checkt.core.checks

import io.dwsoft.checkt.core.Check
import io.dwsoft.checkt.core.LazyErrorMessage
import io.dwsoft.checkt.core.ValidationRule
import io.dwsoft.checkt.core.ValidationRules

object NotBlank : Check<CharSequence> by Check({ it.isNotBlank() }) {
    object Rule : ValidationRule.Descriptor<CharSequence, NotBlank, Rule> {
        override val defaultMessage: LazyErrorMessage<Rule, CharSequence> = { "Value must not be blank" }
    }
}

val ValidationRules<CharSequence>.notBlank: ValidationRule<NotBlank.Rule, CharSequence>
    get() = NotBlank.toValidationRule(NotBlank.Rule)
