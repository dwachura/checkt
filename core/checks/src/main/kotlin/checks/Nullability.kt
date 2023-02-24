package io.dwsoft.checkt.core.checks

import io.dwsoft.checkt.core.Check
import io.dwsoft.checkt.core.LazyErrorMessage
import io.dwsoft.checkt.core.ValidationRule
import io.dwsoft.checkt.core.ValidationBlock
import io.dwsoft.checkt.core.ValidationOf
import io.dwsoft.checkt.core.ValidationRules
import io.dwsoft.checkt.core.ValidationStatus

object NotNull : Check<Any?> by Check({ it != null })

fun <T> ValidationRules<T?>.notBeNull(
    errorMessage: LazyErrorMessage<NotNull, T?> = { "Value must not be null" },
): ValidationRule<T?, NotNull> =
    NotNull.toValidationRule(errorMessage)

suspend fun <T : Any> ValidationOf<T?>.notNullAnd(
    notNullErrorMessage: LazyErrorMessage<NotNull, T?>? = null,
    notNullValidation: ValidationBlock<T>,
): ValidationStatus {
    val rule = notNullErrorMessage?.let { notBeNull(it) } ?: notBeNull()
    return (+rule).whenValid {
        (subject!!) { notNullValidation() }
    }
}

object IsNull : Check<Any?> by Check({ it == null })

fun <T> ValidationRules<T?>.beNull(
    errorMessage: LazyErrorMessage<IsNull, T?> = { "Value must be null" },
): ValidationRule<T?, IsNull> =
    IsNull.toValidationRule(errorMessage)
