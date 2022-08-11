package io.dwsoft.checkt.basic.checks

import io.dwsoft.checkt.core.Check
import io.dwsoft.checkt.core.Check.Context
import io.dwsoft.checkt.core.Check.Params.None
import io.dwsoft.checkt.core.ErrorDetailsBuilder
import io.dwsoft.checkt.core.ValidationDsl
import io.dwsoft.checkt.core.ValidationError
import io.dwsoft.checkt.core.ValidationRule
import io.dwsoft.checkt.core.ValidationScope
import io.dwsoft.checkt.core.errorMessage
import io.dwsoft.checkt.core.toDisplayed
import io.dwsoft.checkt.core.toValidationRule

class NonNull<T : Any?> : Check<T, NonNull.Key, None> {
    override val context = Context.of(Key, None)

    override fun invoke(value: T): Boolean =
        value != null

    object Key : Check.Key
}

fun <V> notBeNull(
    errorDetailsBuilder: ErrorDetailsBuilder<V, NonNull.Key, None> =
        errorMessage { "${validationPath()} must not be null" },
): ValidationRule<V, NonNull.Key, None> =
    NonNull<V>().toValidationRule(errorDetailsBuilder)

context(ValidationScope, V)
infix fun <E : ValidationError<V, NonNull.Key, None>?, V> E.and(
    notNullContext: context(ValidationScope, ValidationDsl) (V & Any).() -> Unit
) {
    this ?: notNullContext(this@ValidationScope, ValidationDsl, this@V!!)
}

class IsNull<T : Any?> : Check<T, IsNull.Key, None> {
    override val context = Context.of(Key, None)

    override fun invoke(value: T): Boolean =
        value == null

    object Key : Check.Key
}

fun <V> beNull(
    errorDetailsBuilder: ErrorDetailsBuilder<V, IsNull.Key, None> =
        errorMessage { "${validationPath()} must be null" },
): ValidationRule<V, IsNull.Key, None> =
    IsNull<V>().toValidationRule(errorDetailsBuilder)
