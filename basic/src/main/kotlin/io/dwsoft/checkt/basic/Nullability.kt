package io.dwsoft.checkt.basic

import io.dwsoft.checkt.core.Check
import io.dwsoft.checkt.core.Check.Params.None
import io.dwsoft.checkt.core.LazyErrorDetails
import io.dwsoft.checkt.core.ValidationError
import io.dwsoft.checkt.core.ValidationRule
import io.dwsoft.checkt.core.ValidationScope
import io.dwsoft.checkt.core.ValidationSpec
import io.dwsoft.checkt.core.toValidationRule

class NonNull<V : Any?> : Check.WithoutParams<V, NonNull<V>> by Check.WithoutParams.delegate(
    implementation = { value -> value != null }
)

fun <V> notBeNull(
    errorDetails: LazyErrorDetails<NonNull<V>, V, None<NonNull<V>>> =
        { "${validationPath()} must not be null" },
): ValidationRule<NonNull<V>, V, None<NonNull<V>>> =
    NonNull<V>().toValidationRule(errorDetails)

// TODO: remove contexts
//context (ValidationScope, V)
//infix fun <E : ValidationError<NonNull<V>, V, None<NonNull<V>>>?, V> E.and(
//    notNullContext: context (ValidationSpec<V>) (V & Any).() -> Unit
//) {
//    this ?: notNullContext(ValidationSpec(this@V!!, this@ValidationScope), this@V!!)
//}

class IsNull<V : Any?> : Check.WithoutParams<V, IsNull<V>> by Check.WithoutParams.delegate(
    implementation = { value -> value == null }
)

fun <V> beNull(
    errorDetails: LazyErrorDetails<IsNull<V>, V, None<IsNull<V>>> =
        { "${validationPath()} must be null" },
): ValidationRule<IsNull<V>, V, None<IsNull<V>>> =
    IsNull<V>().toValidationRule(errorDetails)
