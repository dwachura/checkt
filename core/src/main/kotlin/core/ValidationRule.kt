package io.dwsoft.checkt.core

import kotlin.reflect.KClass

/**
 * Validation rules are objects that combines [validation condition][Check]
 * together with [error message][errorMessage].
 *
 * Custom validation rules may be defined as an extensions of
 * [validation rules "namespace"][ValidationRules] to make them available 
 * easier in validation DSL.
 */
class ValidationRule<D : ValidationRule.Descriptor<V, C>, in V, C : Check<*>> private constructor(
    val descriptor: D,
    val check: C,
    private val errorMessage: LazyErrorMessage<D, out V, C>,
    private val applyCheck: suspend ValidationRule<D, V, C>.(V) -> LazyViolation<D, V, C>,
) {
    suspend fun verify(value: V): LazyViolation<D, @UnsafeVariance V, C> =
        { validationPath -> applyCheck(value)(validationPath) }

    companion object : ValidationRules<Any?> {
        fun <D : Descriptor<V, C>, C : Check<T>, V, T> create(
            descriptor: D,
            check: C,
            errorMessage: LazyErrorMessage<D, V, C>,
            transform: (V) -> T,
        ): ValidationRule<D, V, C> =
            ValidationRule(descriptor, check, errorMessage) { value ->
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

        fun <D : Descriptor<V, C>, C : ParameterizedCheck<T, P>, V, P : ParamsOf<C, P>, T> create(
            descriptor: D,
            check: C,
            errorMessage: LazyErrorMessage<D, V, C>,
            transform: (V) -> T,
        ): ValidationRule<D, V, C> =
            ValidationRule(descriptor, check, errorMessage) { value ->
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

    abstract class Descriptor<in V, C : Check<*>>(val check: Check.Key<C>) {
        constructor(checkClass: KClass<C>) : this(checkClass.checkKey())
        constructor(check: C) : this(check.key)

        val id: String = javaClass.canonicalName
        val name: String = javaClass.simpleName
    }
}

class ErrorMessageBuilderContext<D : ValidationRule.Descriptor<V, C>, V, C : Check<*>>(
    val value: V,
    val context: ValidationContext<D, C>
) {
    operator fun ValidationPath.invoke(separator: String = "."): String =
        joinToString { acc, str -> "$acc$separator$str" }
}

typealias LazyErrorMessage<D, V, C> = ErrorMessageBuilderContext<D, out V, C>.() -> String

typealias LazyViolation<D, V, C> = suspend (ValidationPath) -> Violation<D, V, C>?

/**
 * "Namespace" introduced to provide common and easy access to
 * [ValidationRule]-related functions.
 *
 * Custom rule factories functions could be defined as an extensions
 * of this interface for better access in validation DSL.
 */
interface ValidationRules<out V> {
    fun <T> rule(block: ValidationRules<T>.() -> ValidationRule<*, T, *>):
            ValidationRule<*, T, *> =
        rulesFor<T>().block()

    @Suppress("UNCHECKED_CAST")
    fun <T> rulesFor(): ValidationRules<T> = this as ValidationRules<T>

    fun <D, C> C.toValidationRule(descriptor: D, errorMessage: LazyErrorMessage<D, V, C>):
            ValidationRule<D, @UnsafeVariance V, C>
            where D : ValidationRule.Descriptor<V, C>, C : Check<V> =
        ValidationRule.create(descriptor, this, errorMessage) { it }

    fun <D, C, P> C.toValidationRule(descriptor: D, errorMessage: LazyErrorMessage<D, V, C>):
            ValidationRule<D, @UnsafeVariance V, C>
            where D : ValidationRule.Descriptor<V, C>, C : ParameterizedCheck<V, P>, P : ParamsOf<C, P> =
        ValidationRule.create(descriptor, this, errorMessage) { it }
}
