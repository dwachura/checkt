package io.dwsoft.checkt.core.checks

import io.dwsoft.checkt.core.Check
import io.dwsoft.checkt.core.Check.Params.None
import io.dwsoft.checkt.core.LazyErrorMessage
import io.dwsoft.checkt.core.ValidationRule
import io.dwsoft.checkt.core.toValidationRule

class NonNull<V : Any?> : Check.Parameterless<V, NonNull<V>> by Check.Parameterless.delegate(
    implementation = { value -> value != null }
)

fun <V> notBeNull(
    errorMessage: LazyErrorMessage<NonNull<V>, V, None<NonNull<V>>> =
        { "${validationPath()} must not be null" },
): ValidationRule<NonNull<V>, V, None<NonNull<V>>> =
    NonNull<V>().toValidationRule(errorMessage)

// TODO: remove contexts
//context (ValidationScope, V)
//infix fun <E : Violation<NonNull<V>, V, None<NonNull<V>>>?, V> E.and(
//    notNullContext: context (ValidationSpec<V>) (V & Any).() -> Unit
//) {
//    this ?: notNullContext(ValidationSpec(this@V!!, this@ValidationScope), this@V!!)
//}

class IsNull<V : Any?> : Check.Parameterless<V, IsNull<V>> by Check.Parameterless.delegate(
    implementation = { value -> value == null }
)

fun <V> beNull(
    errorMessage: LazyErrorMessage<IsNull<V>, V, None<IsNull<V>>> =
        { "${validationPath()} must be null" },
): ValidationRule<IsNull<V>, V, None<IsNull<V>>> =
    IsNull<V>().toValidationRule(errorMessage)
