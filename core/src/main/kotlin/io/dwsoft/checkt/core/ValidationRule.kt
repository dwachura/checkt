package io.dwsoft.checkt.core

class ValidationRule<C : Check<V, P, C>, in V, P : Check.Params<C>>(
    val check: C,
    val errorDetails: LazyErrorDetails<C, out V, P>,
)

class ErrorDetailsBuilderContext<C : Check<V, P, C>, V, P : Check.Params<C>> internal constructor(
    val value: V,
    val validationPath: ValidationPath,
    check: C,
) {
    val violatedCheck: Check.Key<C> = check.key
    val validationParams: P = check.params

    operator fun ValidationPath.invoke(separator: String = "."): String =
        validationPath.joinToString { acc, str -> "$acc$separator$str" }
}

typealias LazyErrorDetails<C, V, P> = ErrorDetailsBuilderContext<C, out V, P>.() -> String

fun <C : Check<V, P, C>, V, P : Check.Params<C>> C.toValidationRule(
    errorDetails: LazyErrorDetails<C, V, P>
): ValidationRule<C, V, P>
    = ValidationRule(this, errorDetails)

data class ValidationContext<C : Check<*, P, C>, P : Check.Params<C>>(
    val key: Check.Key<C>,
    val params: P,
)

val <C : Check<*, P, C>, P : Check.Params<C>> ValidationRule<C, *, P>.validationContext: ValidationContext<C, P>
    get() = ValidationContext(check.key, check.params)
