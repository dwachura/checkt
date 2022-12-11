package io.dwsoft.checkt.core

class ValidationRule<C : Check<V, P, C>, in V, P : Check.Params<C>>(
    val check: C,
    val errorMessage: LazyErrorMessage<C, out V, P>,
)

class ErrorMessageBuilderContext<C : Check<V, P, C>, V, P : Check.Params<C>> internal constructor(
    val value: V,
    val validationPath: ValidationPath,
    check: C,
) {
    val violatedCheck: Check.Key<C> = check.key
    val validationParams: P = check.params

    operator fun ValidationPath.invoke(separator: String = "."): String =
        validationPath.joinToString { acc, str -> "$acc$separator$str" }
}

typealias LazyErrorMessage<C, V, P> = ErrorMessageBuilderContext<C, out V, P>.() -> String

fun <C : Check<V, P, C>, V, P : Check.Params<C>> C.toValidationRule(
    errorMessage: LazyErrorMessage<C, V, P>
): ValidationRule<C, V, P>
    = ValidationRule(this, errorMessage)
