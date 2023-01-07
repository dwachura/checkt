package io.dwsoft.checkt.core.checks

import io.dwsoft.checkt.core.Check
import io.dwsoft.checkt.core.Check.Params.None
import io.dwsoft.checkt.core.LazyErrorMessage
import io.dwsoft.checkt.core.ValidationBlock
import io.dwsoft.checkt.core.ValidationOf
import io.dwsoft.checkt.core.ValidationRule
import io.dwsoft.checkt.core.ValidationRules
import io.dwsoft.checkt.core.ValidationStatus

class NonNull<V> : Check.Parameterless<V?, NonNull<V?>> by Check.Parameterless.delegate(
    implementation = { value -> value != null }
)

fun <T> ValidationRules<T?>.notBeNull(
    errorMessage: LazyErrorMessage<NonNull<T?>, T?, None<NonNull<T?>>> =
        { "Value must not be null" },
): ValidationRule<NonNull<T?>, T?, None<NonNull<T?>>> =
    NonNull<T?>().toValidationRule(errorMessage)

suspend fun <T : Any> ValidationOf<T?>.subjectNotNullAnd(
    nonNullErrorMessage: LazyErrorMessage<NonNull<T?>, T?, None<NonNull<T?>>>? = null,
    nonNullValidation: ValidationBlock<T>,
): ValidationStatus {
    val nonNullRule = nonNullErrorMessage?.let { notBeNull(it) } ?: notBeNull()
    return (+nonNullRule).whenValid {
        (subject!!) { nonNullValidation() }
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
