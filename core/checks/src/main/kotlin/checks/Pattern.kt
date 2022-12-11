package io.dwsoft.checkt.core.checks

import io.dwsoft.checkt.core.Check
import io.dwsoft.checkt.core.LazyErrorMessage
import io.dwsoft.checkt.core.ValidationRule
import io.dwsoft.checkt.core.toValidationRule

class Pattern(private val regex: Regex) :
    Check<CharSequence, Pattern.Params, Pattern>
{
    override val params = Params(regex)

    override suspend fun invoke(value: CharSequence): Boolean =
        regex.matches(value)

    data class Params(val regex: Regex) : Check.Params<Pattern>()
}

fun matchRegex(
    regex: Regex,
    errorMessage: LazyErrorMessage<Pattern, CharSequence, Pattern.Params> =
        { "${validationPath()} must match regex '${validationParams.regex}'" },
): ValidationRule<Pattern, CharSequence, Pattern.Params> =
    Pattern(regex).toValidationRule(errorMessage)
