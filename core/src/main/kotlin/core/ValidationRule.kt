package io.dwsoft.checkt.core

/**
 * Validation rules are objects that combines [validation condition][Check]
 * together with [error message][errorMessage].
 *
 * Custom validation rules may be defined as an extensions of
 * [validation rules "namespace"][ValidationRules] to make them available 
 * easier in validation DSL.
 */
class ValidationRule<D : ValidationRule.Descriptor<V, *, D>, in V> private constructor(
    val descriptor: D,
    private val errorMessage: LazyErrorMessage<D, out V>,
    private val applyCheck: suspend ValidationRule<D, V>.(V) -> LazyViolation<D, V>,
) {
    suspend fun verify(value: V): LazyViolation<D, @UnsafeVariance V> =
        { validationPath -> applyCheck(value)(validationPath) }

    fun overrideErrorMessage(errorMessage: LazyErrorMessage<D, @UnsafeVariance V>): ValidationRule<D, V> =
        ValidationRule(descriptor, errorMessage, applyCheck)

    companion object : ValidationRules<Any?> {
        fun <D : Descriptor<V, C, D>, C : Check<T>, V, T> create(
            descriptor: D,
            check: C,
            transform: (V) -> T,
        ): ValidationRule<D, V> =
            ValidationRule(descriptor, descriptor.defaultMessage) { value ->
                val result = check(transform(value))
                return@ValidationRule { validationPath: ValidationPath ->
                    result.takeUnless { it.passed }
                        ?.let {
                            val context = ValidationContext.create(descriptor, validationPath)
                            val msg = ErrorMessageBuilderContext(value, context).errorMessage()
                            Violation(value, context, msg)
                        }
                }
            }

        fun <D : Descriptor<V, C, D>, C : ParameterizedCheck<T, P>, V, P : ParamsOf<C, P>, T> create(
            descriptor: D,
            check: C,
            transform: (V) -> T,
        ): ValidationRule<D, V> =
            ValidationRule(descriptor, descriptor.defaultMessage) { value ->
                val result = check(transform(value))
                return@ValidationRule { validationPath: ValidationPath ->
                    result.takeUnless { it.passed }
                        ?.let {
                            val context = ValidationContext.create(descriptor, it.params, validationPath)
                            val msg = ErrorMessageBuilderContext(value, context).errorMessage()
                            Violation(value, context, msg)
                        }
                }
            }
    }

    interface Descriptor<in V, C : Check<*>, SELF : Descriptor<V, C, SELF>> {
        val defaultMessage: LazyErrorMessage<SELF, V>
    }
}

val ValidationRule.Descriptor<*, *, *>.key: String
    get() = javaClass.canonicalName

val ValidationRule.Descriptor<*, *, *>.name: String
    get() = javaClass.simpleName

class ErrorMessageBuilderContext<D : ValidationRule.Descriptor<V, *, D>, V>(
    val value: V,
    val context: ValidationContext<D>
) {
    operator fun ValidationPath.invoke(separator: String = "."): String =
        joinToString { acc, str -> "$acc$separator$str" }
}

typealias LazyErrorMessage<D, V> = ErrorMessageBuilderContext<D, out V>.() -> String

typealias LazyViolation<D, V> = suspend (ValidationPath) -> Violation<D, V>?

/**
 * "Namespace" introduced to provide common and easy access to
 * [ValidationRule]-related functions.
 *
 * Custom rule factories functions could be defined as an extensions
 * of this interface for better access in validation DSL.
 */
interface ValidationRules<out V> {
    fun <T> rule(block: ValidationRules<T>.() -> ValidationRule<*, T>):
            ValidationRule<*, T> =
        rulesFor<T>().block()

    @Suppress("UNCHECKED_CAST")
    fun <T> rulesFor(): ValidationRules<T> = this as ValidationRules<T>

    fun <C, D> C.toValidationRule(descriptor: D):
            ValidationRule<D, @UnsafeVariance V>
            where C : Check<V>, D : ValidationRule.Descriptor<V, C, D> =
        ValidationRule.create(descriptor, this) { it }

    fun <C, D, P> C.toValidationRule(descriptor: D):
            ValidationRule<D, @UnsafeVariance V>
            where C : ParameterizedCheck<V, P>, D : ValidationRule.Descriptor<V, C, D>, P : ParamsOf<C, P> =
        ValidationRule.create(descriptor, this) { it }

    fun <C> C.toValidationRule(): ValidationRule<C, @UnsafeVariance V>
            where C : Check<V>, C : ValidationRule.Descriptor<V, C, C> =
        ValidationRule.create(this, this) { it }

    fun <C, P> C.toValidationRule(): ValidationRule<C, @UnsafeVariance V>
            where C : ParameterizedCheck<V, P>, C : ValidationRule.Descriptor<V, C, C>, P : ParamsOf<C, P> =
        ValidationRule.create(this, this) { it }

    infix fun <D, V> ValidationRule<D, V>.withMessage(errorMessage: LazyErrorMessage<D, V>): ValidationRule<D, V>
            where D : ValidationRule.Descriptor<V, *, D> =
        overrideErrorMessage(errorMessage)
}
