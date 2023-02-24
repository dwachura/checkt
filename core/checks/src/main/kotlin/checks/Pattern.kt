package io.dwsoft.checkt.core.checks

import io.dwsoft.checkt.core.LazyErrorMessage
import io.dwsoft.checkt.core.ParameterizedCheck
import io.dwsoft.checkt.core.ParamsOf
import io.dwsoft.checkt.core.ValidationRule
import io.dwsoft.checkt.core.ValidationRules
import io.dwsoft.checkt.core.and
import io.dwsoft.checkt.core.params

class Pattern(regex: Regex) : ParameterizedCheck<CharSequence, Pattern.Params> by (
        Params(regex) and { regex.matches(it) }
) {
    data class Params(val regex: Regex) : ParamsOf<Pattern, Params>
}

fun ValidationRules<CharSequence>.matchesRegex(
    regex: Regex,
    errorMessage: LazyErrorMessage<Pattern, CharSequence> =
        { "Value must match regex '${context.params.regex}'" },
): ValidationRule<CharSequence, Pattern> =
    Pattern(regex).toValidationRule(errorMessage)
