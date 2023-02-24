package io.dwsoft.checkt.core

/**
 * Validation rules are objects that combines [validation condition][Check]
 * together with [error message][errorMessage].
 *
 * Custom validation rules should be defined as an extensions of
 * [validation rules "namespace"][ValidationRules].
 */
class ValidationRule<in V, C : Check<*>> private constructor(
    val check: C,
    val errorMessage: LazyErrorMessage<C, out V>,
    private val applyCheck: suspend (V, C) -> Boolean,
    private val validationContext: (ValidationPath) -> ValidationContext<C>,
) {
    suspend fun verify(value: V): LazyViolation<C, @UnsafeVariance V> =
        { validationPath ->
            when (applyCheck(value, check)) {
                false -> {
                    val context = validationContext(validationPath)
                    val msg = ErrorMessageBuilderContext(value, context).errorMessage()
                    Violation(value, context, msg)
                }
                true -> null
            }
        }

    companion object : ValidationRules<Any?> {
        fun <C : Check<T>, V, T> create(
            check: C,
            errorMessage: LazyErrorMessage<C, V>,
            transform: (V) -> T,
        ): ValidationRule<V, C> =
            create(check, errorMessage, transform) { validationPath ->
                ValidationContext.create(check, validationPath)
            }

        fun <C : ParameterizedCheck<T, P>, V, P : ParamsOf<C, P>, T> create(
            check: C,
            errorMessage: LazyErrorMessage<C, V>,
            transform: (V) -> T,
        ): ValidationRule<V, C> =
            create(check, errorMessage, transform) { validationPath ->
                ValidationContext.create(check, validationPath)
            }

        private fun <C : Check<T>, V, T> create(
            check: C,
            errorMessage: LazyErrorMessage<C, V>,
            transform: (V) -> T,
            validationContext: (ValidationPath) -> ValidationContext<C>,
        ): ValidationRule<V, C> =
            ValidationRule(check, errorMessage, { v, c -> c(transform(v)) }, validationContext)
    }
}

class ErrorMessageBuilderContext<C : Check<*>, V>(
    val value: V,
    val context: ValidationContext<C>
) {
    operator fun ValidationPath.invoke(separator: String = "."): String =
        joinToString { acc, str -> "$acc$separator$str" }
}

typealias LazyErrorMessage<C, V> = ErrorMessageBuilderContext<C, out V>.() -> String

typealias LazyViolation<C, V> = suspend (ValidationPath) -> Violation<C, V>?

/**
 * "Namespace" introduced to provide common and easy access to
 * [ValidationRule]-related functions.
 *
 * All custom rule factories functions should be defined as an extensions
 * of this interface.
 */
sealed interface ValidationRules<out V> {
    fun <T> rule(block: ValidationRules<T>.() -> ValidationRule<T, *>):
            ValidationRule<T, *> =
        rulesFor<T>().block()

    @Suppress("UNCHECKED_CAST")
    fun <T> rulesFor(): ValidationRules<T> = this as ValidationRules<T>

    fun <C> C.toValidationRule(errorMessage: LazyErrorMessage<C, V>):
            ValidationRule<@UnsafeVariance V, C>
            where C : Check<V> =
        ValidationRule.create(this, errorMessage) { it }

    fun <C, P> C.toValidationRule(errorMessage: LazyErrorMessage<C, V>):
            ValidationRule<@UnsafeVariance V, C>
            where C : ParameterizedCheck<V, P>, P : ParamsOf<C, P> =
        ValidationRule.create(this, errorMessage) { it }
}
