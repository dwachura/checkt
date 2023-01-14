package io.dwsoft.checkt.core.checks

import io.dwsoft.checkt.core.Check
import io.dwsoft.checkt.core.Check.Params.None
import io.dwsoft.checkt.core.LazyErrorMessage
import io.dwsoft.checkt.core.ValidationBlock
import io.dwsoft.checkt.core.ValidationOf
import io.dwsoft.checkt.core.ValidationRule
import io.dwsoft.checkt.core.ValidationRules
import io.dwsoft.checkt.core.ValidationStatus

class NotNull<V> : Check.Parameterless<V?, NotNull<V?>> by Check.Parameterless.delegate(
    implementation = { value -> value != null }
)

fun <T> ValidationRules<T?>.notBeNull(
    errorMessage: LazyErrorMessage<NotNull<T?>, T?, None<NotNull<T?>>> =
        { "Value must not be null" },
): ValidationRule<NotNull<T?>, T?, None<NotNull<T?>>> =
    NotNull<T?>().toValidationRule(errorMessage)

suspend fun <T : Any> ValidationOf<T?>.notNullAnd(
    notNullErrorMessage: LazyErrorMessage<NotNull<T?>, T?, None<NotNull<T?>>>? = null,
    notNullValidation: ValidationBlock<T>,
): ValidationStatus {
    val rule = notNullErrorMessage?.let { notBeNull(it) } ?: notBeNull()
    return (+rule).whenValid {
        (subject!!) { notNullValidation() }
    }
}

class IsNull<V> : Check.Parameterless<V?, IsNull<V?>> by Check.Parameterless.delegate(
    implementation = { value -> value == null }
)

fun <T> ValidationRules<T?>.beNull(
    errorMessage: LazyErrorMessage<IsNull<T?>, T?, None<IsNull<T?>>> =
        { "Value must be null" },
): ValidationRule<IsNull<T?>, T?, None<IsNull<T?>>> =
    IsNull<T?>().toValidationRule(errorMessage)
