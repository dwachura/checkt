package io.dwsoft.checkt.core

/**
 * Validation rules are objects that combines [validation condition][Check]
 * together with [error message][errorMessage].
 *
 * Custom validation rules should be defined as an extensions of
 * [validation rules "namespace"][ValidationRules].
 */
class ValidationRule<C : Check<V, P, C>, in V, P : Check.Params<C>> private constructor(
    val check: C,
    val errorMessage: LazyErrorMessage<C, out V, P>,
) {
    suspend fun verify(value: V): LazyViolation<C, @UnsafeVariance V, P> =
        { validationPath ->
            when (check(value)) {
                false -> {
                    val msg = ErrorMessageBuilderContext(value, validationPath, check).errorMessage()
                    val errorContext = Violation.Context(check, validationPath)
                    Violation(value, errorContext, msg)
                }
                true -> null
            }
        }

    companion object : ValidationRules<Any?> {
        fun <C : Check<V, P, C>, V, P : Check.Params<C>> create(
            check: C,
            errorMessage: LazyErrorMessage<C, V, P>,
        ): ValidationRule<C, V, P> =
            ValidationRule(check, errorMessage)
    }
}

class ErrorMessageBuilderContext<C : Check<V, P, C>, V, P : Check.Params<C>>
internal constructor(
    val value: V,
    val validationPath: ValidationPath,
    check: C,
) {
    val violatedCheck: Check.Key<C> = check.key
    val validationParams: P = check.params

    operator fun ValidationPath.invoke(separator: String = "."): String =
        validationPath.joinToString { acc, str -> "$acc$separator$str" }
}

typealias LazyErrorMessage<C, V, P> =
        ErrorMessageBuilderContext<C, out V, P>.() -> String

typealias LazyViolation<C, V, P> =
        suspend (ValidationPath) -> Violation<C, V, P>?

/**
 * "Namespace" introduced to provide common and easy access to
 * [ValidationRule]-related functions.
 *
 * All custom rule factories functions should be defined as an extensions
 * of this interface.
 */
sealed interface ValidationRules<V> {
    fun <T> rule(
        block: ValidationRules<T>.() -> ValidationRule<*, T, *>
    ): ValidationRule<*, T, *> = rulesFor<T>().block()

    @Suppress("UNCHECKED_CAST")
    fun <T> rulesFor(): ValidationRules<T> = this as ValidationRules<T>

    fun <C : Check<V, P, C>, P : Check.Params<C>> C.toValidationRule(
        errorMessage: LazyErrorMessage<C, V, P>
    ): ValidationRule<C, V, P> =
        ValidationRule.create(this, errorMessage)
}
