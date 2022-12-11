package io.dwsoft.checkt.core.checks

import io.dwsoft.checkt.core.Check
import io.dwsoft.checkt.core.Check.Params.None
import io.dwsoft.checkt.core.LazyErrorMessage
import io.dwsoft.checkt.core.ValidationRule
import io.dwsoft.checkt.core.ValidationRules

class NonNull<V : Any?> : Check.Parameterless<V, NonNull<V>> by Check.Parameterless.delegate(
    implementation = { value -> value != null }
)

fun <T> ValidationRules<T>.notBeNull(
    errorMessage: LazyErrorMessage<NonNull<T>, T, None<NonNull<T>>> =
        { "${validationPath()} must not be null" },
): ValidationRule<NonNull<T>, T, None<NonNull<T>>> =
    NonNull<T>().toValidationRule(errorMessage)

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

fun <T> ValidationRules<T>.beNull(
    errorMessage: LazyErrorMessage<IsNull<T>, T, None<IsNull<T>>> =
        { "${validationPath()} must be null" },
): ValidationRule<IsNull<T>, T, None<IsNull<T>>> =
    IsNull<T>().toValidationRule(errorMessage)
