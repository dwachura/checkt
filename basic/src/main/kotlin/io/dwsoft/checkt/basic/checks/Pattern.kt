package io.dwsoft.checkt.basic.checks

import io.dwsoft.checkt.core.Check
import io.dwsoft.checkt.core.Check.Context
import io.dwsoft.checkt.core.ErrorDetailsBuilder
import io.dwsoft.checkt.core.ValidationRule
import io.dwsoft.checkt.core.errorMessage
import io.dwsoft.checkt.core.toDisplayed
import io.dwsoft.checkt.core.toValidationRule

class Pattern(private val regex: Regex) :
    Check<CharSequence, Pattern.Key, Pattern.Params>
{
    override val context = Context.of(Key, Params(regex))

    override fun invoke(value: CharSequence): Boolean =
        regex.matches(value)

    object Key : Check.Key
    data class Params(val regex: Regex) : Check.Params()
}

fun matchRegex(
    regex: Regex,
    errorDetailsBuilder: ErrorDetailsBuilder<CharSequence, Pattern.Key, Pattern.Params> =
        errorMessage { "${validationPath()} must match regex '${validationParams.regex}'" },
): ValidationRule<CharSequence, Pattern.Key, Pattern.Params> =
    Pattern(regex).toValidationRule(errorDetailsBuilder)
