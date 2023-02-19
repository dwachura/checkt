package io.dwsoft.checkt.core.checks

import io.dwsoft.checkt.core.Check
import io.dwsoft.checkt.core.LazyErrorMessage
import io.dwsoft.checkt.core.ValidationBlock
import io.dwsoft.checkt.core.ValidationOf
import io.dwsoft.checkt.core.ValidationRule
import io.dwsoft.checkt.core.ValidationRules
import io.dwsoft.checkt.core.ValidationStatus

class NotNull<V> : Check<V?> by Check.delegate({ value ->
    value != null
})

fun <T> ValidationRules<T?>.notBeNull(
    errorMessage: LazyErrorMessage<NotNull<T?>, T?> = { "Value must not be null" },
): ValidationRule<NotNull<T?>, T?> =
    NotNull<T?>().toValidationRule(errorMessage)

suspend fun <T : Any> ValidationOf<T?>.notNullAnd(
    notNullErrorMessage: LazyErrorMessage<NotNull<T?>, T?>? = null,
    notNullValidation: ValidationBlock<T>,
): ValidationStatus {
    val rule = notNullErrorMessage?.let { notBeNull(it) } ?: notBeNull()
    return (+rule).whenValid {
        (subject!!) { notNullValidation() }
    }
}

class IsNull<V> : Check<V?> by Check.delegate({ value ->
    value == null
})

fun <T> ValidationRules<T?>.beNull(
    errorMessage: LazyErrorMessage<IsNull<T?>, T?> = { "Value must be null" },
): ValidationRule<IsNull<T?>, T?> =
    IsNull<T?>().toValidationRule(errorMessage)
