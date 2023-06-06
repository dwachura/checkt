package io.dwsoft.checkt.core.checks

import io.dwsoft.checkt.core.Check
import io.dwsoft.checkt.core.LazyErrorMessage
import io.dwsoft.checkt.core.ValidationRule
import io.dwsoft.checkt.core.ValidationBlock
import io.dwsoft.checkt.core.ValidationOf
import io.dwsoft.checkt.core.ValidationRules
import io.dwsoft.checkt.core.ValidationStatus
import io.dwsoft.checkt.core.key

object NotNull : Check<Any?> by Check({ it != null }) {
    object RuleDescriptor : ValidationRule.Descriptor<Any?, NotNull>(Check.key())
}

fun <T> ValidationRules<T?>.notBeNull(
    errorMessage: LazyErrorMessage<NotNull.RuleDescriptor, T?, NotNull> = { "Value must not be null" },
): ValidationRule<NotNull.RuleDescriptor, T?, NotNull> =
    NotNull.toValidationRule(NotNull.RuleDescriptor, errorMessage)

suspend fun <T : Any> ValidationOf<T?>.notNullAnd(
    notNullErrorMessage: LazyErrorMessage<NotNull.RuleDescriptor, T?, NotNull>? = null,
    notNullValidation: ValidationBlock<T>,
): ValidationStatus {
    val rule = notNullErrorMessage?.let { notBeNull(it) } ?: notBeNull()
    return (+rule).whenValid {
        (subject!!) { notNullValidation() }
    }
}

object IsNull : Check<Any?> by Check({ it == null }) {
    object RuleDescriptor : ValidationRule.Descriptor<Any?, IsNull>(Check.key())
}

fun <T> ValidationRules<T?>.beNull(
    errorMessage: LazyErrorMessage<IsNull.RuleDescriptor, T?, IsNull> = { "Value must be null" },
): ValidationRule<IsNull.RuleDescriptor, T?, IsNull> =
    IsNull.toValidationRule(IsNull.RuleDescriptor, errorMessage)
