package io.dwsoft.checkt.core

import kotlin.reflect.KProperty0

/*
 * TODO:
 *  - readme
 *  - add dsl maker ???
 *  - fail-fast mode
 */

/**
 * Validation specification represents a reusable validation logic definition,
 * i.e. a function validating value under named scope.
 */
typealias ValidationSpecification<T> = suspend T.(NotBlankString?) -> ValidationResult

/**
 * Entry point of defining [validation logic][ValidationSpecification] for values
 * of type [T].
 */
fun <T> validation(validationBlock: ValidationBlock<T>): ValidationSpecification<T> =
    spec@{ namedAs ->
        validateCatching {
            ValidationScope(ValidationPath(namedAs)).apply {
                toValidationOf(this@spec)
                    .validationBlock()
                    .getOrThrow()
            }.status
        }
    }

/**
 * Shortcut for immediate invocation of [validation specification][validation].
 */
suspend fun <T> T.validate(
    namedAs: NotBlankString? = null,
    validationBlock: ValidationBlock<T>,
) = validation(validationBlock)(this, namedAs)

/**
 * Validation logic builder/DSL.
 *
 * It creates validation context composed of validated value, i.e. [subject] together
 * with a [named scope][ValidationScope] within which defined rules are executed.
 *
 * Besides providing more readable and user-friendly DSL functions than
 * [ValidationScope] it also performs validation operations in an exception-safely
 * manner, as each validation operation defined by the DSL returns
 * [ValidationBlockReturnable] instances. They may be viewed as
 * [ValidationResult]-based equivalents of [ValidationStatus]-based
 * [ValidationScopeBlockReturnable]s which are returned by [ValidationScope]'s
 * functions.
 *
 * [ValidationOf] is also [validation rules "namespace"][ValidationRules], giving
 * access to the variety of [validation rules][ValidationRule] DSL functions.
 *
 * Instances of this class are also of [ValidationOf.Internals] type that
 * provides access to the internal implementation properties (like [validation scope]
 * [Internals.scope]). This trick lets to hide internal stuff when DSL is used to
 * defining validation logic, but let them be available when needed (e.g. to define
 * custom extension functions).
 */
sealed class ValidationOf<V>(val subject: V) : ValidationRules<V> {
    /**
     * Checks the [currently validated value][subject] against given
     * [validation rule][this].
     */
    suspend operator fun <C : Check<V, P, C>, P : Check.Params<C>>
            ValidationRule<C, V, P>.unaryPlus(): ValidationBlockReturnable =
        internalsGate.validationBlock {
            scope.verifyValue(subject, this@unaryPlus).status
        }

    /**
     * Validates value (passed as a receiver) against given [validation logic]
     * [block], executed under the current context's [naming]
     * [ValidationScope.validationPath].
     */
    suspend operator fun <T> T.invoke(
        block: ValidationBlock<T>
    ): ValidationBlockReturnable =
        internalsGate.validationBlock {
            scope.validate {
                returning(
                    toValidationOf(this@invoke).block()
                )
            }.status
        }

    /**
     * Validate given [value][Named] against [validation logic][block] into a new,
     * [named][Named.name] [context][ValidationOf] relative to the current one.
     *
     * New naming must be unique under the current context, [unrecoverable error]
     * [ValidationResultSettings.unrecoverableErrorTypes] is thrown otherwise (see
     * [ValidationScope.validate]).
     */
    suspend infix fun <T> Named<T>.require(
        block: ValidationBlock<T>
    ): ValidationBlockReturnable =
        internalsGate.validationBlock {
            scope.validate(name.asSegment()) {
                returning(
                    toValidationOf(value).block()
                )
            }.status
        }

    /**
     * "Overloaded" version of [Named.require] function that takes property as
     * a receiver. A new scope's name and value to validate is taken from the
     * property specified ([KProperty0.name] and [KProperty0.get] respectively).
     */
    suspend infix fun <T> KProperty0<T>.require(
        block: ValidationBlock<T>
    ): ValidationBlockReturnable =
        toNamed().require(block)

    /**
     * DSL version of [runWhenValid] making [current context][ValidationOf]
     * available into passed [validation block][block].
     */
    suspend fun ValidationResult.whenValid(
        block: ValidationBlock<V>
    ): ValidationBlockReturnable =
        internalsGate.validationBlock {
            runWhenValid { block() }.getOrThrow()
        }

    /**
     * DSL version of [runWhenFailureOfType] making [current context][ValidationOf]
     * available into passed [validation block][block].
     */
    suspend inline fun <reified E : Throwable> ValidationResult.recoverFrom(
        crossinline block: ValidationBlock1<V, E>,
    ): ValidationBlockReturnable =
        (this@ValidationOf as Internals).validationBlock {
            runWhenFailureOfType<E> { block(it) }.getOrThrow()
        }

    sealed class Internals<V>(
        subject: V,
        val scope: ValidationScope
    ) : ValidationOf<V>(subject)
}

/**
 * Validates each element of given iterable (being a [subject][ValidationOf.subject]
 * of a current context) against provided [validation rules][block].
 * Each element is validated into new scope indexed according to its position in the
 * iterable. Index of currently validated element is passed to the validation block
 * as a lambda parameter.
 */
suspend fun <T : Iterable<EL>, EL> ValidationOf<T>.eachElement(
    block: ValidationBlock1<EL, Int>,
): ValidationBlockReturnable =
    internalsGate.validationBlock {
        subject.mapIndexed { idx, value ->
            scope.validate(idx.asIndex()) {
                returning(
                    toValidationOf(value).block(idx)
                )
            }.status
        }.fold()
    }

/**
 * Validates each entry of given map (being a [subject][ValidationOf.subject] of
 * a current context) against provided validation rules.
 *
 * Rules can be defined for separately entry's key and value (the former is optional).
 * In both cases "opposite" entry side (value in case of a key, and vice-versa) is
 * passed to the validation lambda as a parameter.
 *
 * Each entry is validated into new scope indexed with entry key transformed by the
 * given [transforming function][displayingKeysAs]. Entry's key and value also have
 * their own scopes created named "key" and "value" respectively.
 */
suspend fun <T : Map<K, V>, K, V> ValidationOf<T>.eachEntry(
    displayingKeysAs: (key: K) -> NotBlankString = { key -> !(key.toString()) },
    keyValidation: ValidationBlock1<K, V>? = null,
    valueValidation: ValidationBlock1<V, K>,
): ValidationBlockReturnable =
    internalsGate.validationBlock {
        subject.map { entry ->
            val key = entry.key
            val value = entry.value
            scope.validate(displayingKeysAs(key).asKey()) {
                val entryValidationResult = validateCatching {
                    val valueValidationStatus = validate((!"value").asSegment()) {
                        returning(
                            toValidationOf(value).valueValidation(key)
                        )
                    }.status
                    val keyValidationStatus = keyValidation?.let {
                        validate((!"key").asSegment()) {
                            returning(
                                toValidationOf(key).keyValidation(value)
                            )
                        }
                    }?.status ?: ValidationStatus.Valid
                    valueValidationStatus + keyValidationStatus
                }
                returning(entryValidationResult)
            }.status
        }.fold()
    }

/**
 * Utility function for defining functions complaint to the [validation DSL]
 * [ValidationOf], i.e. that can serve as [terminators of a validation blocks]
 * [ValidationBlockReturnable].
 */
suspend fun <T> ValidationOf.Internals<T>.validationBlock(
    block: suspend ValidationOf.Internals<T>.() -> ValidationStatus
): ValidationBlockReturnable =
    ValidationBlockReturnableInternal(
        validateCatching { block() }
    )

/**
 * Shorter alias for [validated subject][ValidationOf.subject], that can be
 * more convenient to use to retrieve its properties to validate.
 */
val <T> ValidationOf<T>.the: T
    get() = subject

typealias ValidationBlock<T> =
        suspend ValidationOf<T>.() -> ValidationBlockReturnable

typealias ValidationBlock1<T, T2> =
        suspend ValidationOf<T>.(T2) -> ValidationBlockReturnable

sealed interface ValidationBlockReturnable : ValidationResult

private val <T> ValidationOf<T>.internalsGate: ValidationOf.Internals<T>
    get() = this as ValidationOf.Internals<T>

private class ValidationOfInternal<V>(
    subject: V,
    scope: ValidationScope
) : ValidationOf.Internals<V>(subject, scope)

private fun <T> ValidationScope.toValidationOf(value: T): ValidationOf<T> =
    ValidationOfInternal(value, this)

private class ValidationBlockReturnableInternal(
    result: ValidationResult
) : ValidationBlockReturnable, ValidationResult by result

private suspend fun ValidationScope.returning(
    result: ValidationResult
): ValidationScopeBlockReturnable =
    scopeBlock { result.getOrThrow() }
