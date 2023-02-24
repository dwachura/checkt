package io.dwsoft.checkt.core.checks

import io.dwsoft.checkt.core.Check
import io.dwsoft.checkt.core.LazyErrorMessage
import io.dwsoft.checkt.core.ValidationRule
import io.dwsoft.checkt.core.ValidationRules
import io.dwsoft.checkt.core.params

// TODO: tests

object NotEmpty : Check<CharSequence> by Check({ it.isNotEmpty() })

fun ValidationRules<CharSequence>.notEmpty(
    errorMessage: LazyErrorMessage<NotEmpty, CharSequence> =
        { "Value must not be empty" },
) : ValidationRule<CharSequence, NotEmpty> =
    NotEmpty.toValidationRule(errorMessage)

object NotBlank : Check<CharSequence> by Check({ it.isNotBlank() })

fun ValidationRules<CharSequence>.notBlank(
    errorMessage: LazyErrorMessage<NotBlank, CharSequence> =
        { "Value must not be blank" },
) : ValidationRule<CharSequence, NotBlank> =
    NotBlank.toValidationRule(errorMessage)

// TODO: example of rule creation for different value type
fun ValidationRules<CharSequence>.hasLengthAtLeast(
    minLength: Int,
    errorMessage: LazyErrorMessage<GreaterThanOrEqual<Int>, CharSequence> =
        { "Value must have at least ${context.params.min} characters" },
) : ValidationRule<CharSequence, GreaterThanOrEqual<Int>> =
    ValidationRule.create(GreaterThanOrEqual(minLength), errorMessage) { it.length }
