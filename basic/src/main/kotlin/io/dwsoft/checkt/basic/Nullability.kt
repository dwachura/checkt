package io.dwsoft.checkt.basic

import io.dwsoft.checkt.core.Check
import io.dwsoft.checkt.core.Check.Context
import io.dwsoft.checkt.core.Check.Params.None
import io.dwsoft.checkt.core.ErrorDetailsBuilder
import io.dwsoft.checkt.core.ValidationError
import io.dwsoft.checkt.core.ValidationRule
import io.dwsoft.checkt.core.ValidationScope
import io.dwsoft.checkt.core.ValidationScopeDsl
import io.dwsoft.checkt.core.toValidationRule

class NonNull<T : Any?> : Check<T, NonNull.Key, None> {
    override val context = Context.of(Key, None)

    override fun invoke(value: T): Boolean =
        value != null

    object Key : Check.Key
}

fun <V> notBeNull(
    errorDetailsBuilder: ErrorDetailsBuilder<V, NonNull.Key, None> =
        { "${validationPath()} must not be null" },
): ValidationRule<V, NonNull.Key, None> =
    NonNull<V>().toValidationRule(errorDetailsBuilder)

context (ValidationScope, V)
infix fun <E : ValidationError<V, NonNull.Key, None>?, V> E.and(
    notNullContext: context (ValidationScope, ValidationScopeDsl) (V & Any).() -> Unit
) {
    this ?: notNullContext(this@ValidationScope, ValidationScopeDsl(), this@V!!)
}

class IsNull<T : Any?> : Check<T, IsNull.Key, None> {
    override val context = Context.of(Key, None)

    override fun invoke(value: T): Boolean =
        value == null

    object Key : Check.Key
}

fun <V> beNull(
    errorDetailsBuilder: ErrorDetailsBuilder<V, IsNull.Key, None> =
        { "${validationPath()} must be null" },
): ValidationRule<V, IsNull.Key, None> =
    IsNull<V>().toValidationRule(errorDetailsBuilder)
