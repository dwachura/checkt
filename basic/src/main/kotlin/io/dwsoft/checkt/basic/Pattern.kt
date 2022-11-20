package io.dwsoft.checkt.basic

import io.dwsoft.checkt.core.Check
import io.dwsoft.checkt.core.ErrorDetailsBuilder
import io.dwsoft.checkt.core.ValidationRule
import io.dwsoft.checkt.core.toValidationRule

class Pattern(private val regex: Regex) :
    Check<CharSequence, Pattern.Params, Pattern>
{
    override val params = Params(regex)

    override fun invoke(value: CharSequence): Boolean =
        regex.matches(value)

    data class Params(val regex: Regex) : Check.Params<Pattern>()
}

fun matchRegex(
    regex: Regex,
    errorDetailsBuilder: ErrorDetailsBuilder<Pattern, CharSequence, Pattern.Params> =
        { "${validationPath()} must match regex '${validationParams.regex}'" },
): ValidationRule<Pattern, CharSequence, Pattern.Params> =
    Pattern(regex).toValidationRule(errorDetailsBuilder)
