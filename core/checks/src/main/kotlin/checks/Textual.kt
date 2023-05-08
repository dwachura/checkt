package io.dwsoft.checkt.core.checks

import io.dwsoft.checkt.core.Check
import io.dwsoft.checkt.core.LazyErrorMessage
import io.dwsoft.checkt.core.ValidationRule
import io.dwsoft.checkt.core.ValidationRules

object NotBlank : Check<CharSequence> by Check({ it.isNotBlank() })

fun ValidationRules<CharSequence>.notBlank(
    errorMessage: LazyErrorMessage<NotBlank, CharSequence> =
        { "Value must not be blank" },
) : ValidationRule<CharSequence, NotBlank> =
    NotBlank.toValidationRule(errorMessage)
