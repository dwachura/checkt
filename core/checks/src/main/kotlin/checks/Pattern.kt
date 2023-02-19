package io.dwsoft.checkt.core.checks

import io.dwsoft.checkt.core.LazyErrorMessage
import io.dwsoft.checkt.core.ParameterizedCheck
import io.dwsoft.checkt.core.ParamsOf
import io.dwsoft.checkt.core.ValidationRule
import io.dwsoft.checkt.core.ValidationRules
import io.dwsoft.checkt.core.params

class Pattern(private val regex: Regex) :
    ParameterizedCheck<CharSequence, Pattern.Params>
{
    override val params = Params(regex)

    override suspend fun invoke(value: CharSequence): Boolean =
        regex.matches(value)

    data class Params(val regex: Regex) : ParamsOf<Pattern, Params>
}

fun ValidationRules<CharSequence>.matchesRegex(
    regex: Regex,
    errorMessage: LazyErrorMessage<Pattern, CharSequence> =
        { "Value must match regex '${context.params.regex}'" },
): ValidationRule<Pattern, CharSequence> =
    Pattern(regex).toValidationRule(errorMessage)
