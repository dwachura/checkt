package io.dwsoft.checkt.core.checks

import io.dwsoft.checkt.core.Check
import io.dwsoft.checkt.core.LazyErrorMessage
import io.dwsoft.checkt.core.ValidationBlock
import io.dwsoft.checkt.core.ValidationOf
import io.dwsoft.checkt.core.ValidationRule
import io.dwsoft.checkt.core.ValidationRules
import io.dwsoft.checkt.core.ValidationStatus

object NotNull : Check<Any?> by Check({ it != null }) {
    object Rule : ValidationRule.Descriptor<Any?, NotNull, Rule> {
        override val defaultMessage: LazyErrorMessage<Rule, Any?> = { "Value must not be null" }
    }
}

val <T> ValidationRules<T?>.notBeNull: ValidationRule<NotNull.Rule, T?>
    get() = NotNull.toValidationRule(NotNull.Rule)

suspend fun <T : Any> ValidationOf<T?>.notNullAnd(
    notNullErrorMessage: LazyErrorMessage<NotNull.Rule, T?>? = null,
    notNullValidation: ValidationBlock<T>,
): ValidationStatus {
    val rule = notNullErrorMessage?.let { notBeNull withMessage(it)  } ?: notBeNull
    return (+rule).whenValid {
        (subject!!) { notNullValidation() }
    }
}

object IsNull : Check<Any?> by Check({ it == null }) {
    object Rule : ValidationRule.Descriptor<Any?, IsNull, Rule> {
        override val defaultMessage: LazyErrorMessage<Rule, Any?> = { "Value must be null" }
    }
}

val <T> ValidationRules<T?>.beNull: ValidationRule<IsNull.Rule, T?>
    get() = IsNull.toValidationRule(IsNull.Rule)
