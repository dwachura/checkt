package io.dwsoft.checkt.core

import kotlin.reflect.KProperty0

/*
 * TODO:
 *  - correct docks after scope refactor + tests
 *  - duplicate naming detection
 *  - readme
 *  - exhaustive tests of error handling in regards to error catching and converting to Result
 *  - tests and refactoring collection/map validation in regards of Result
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
 * Each validation operation defined by the [validation DSL][ValidationOf] is run with
 * [exception catching in place][runCatching] and should respond with a
 * [status][ValidationResult] that represent either exceptional completion
 * or successful one resulting with an actual [validation status][ValidationStatus].
 *
 * [ValidationOf] is also [validation rules "namespace"][ValidationRules], giving
 * access to the variety of [validation rules][ValidationRule] DSL functions.
 */
sealed class ValidationOf<V>(val subject: V) : ValidationRules<V> {
    /**
     * Checks the [currently validated value][subject] against given
     * [validation rule][this].
     */
    suspend operator fun <C : Check<V, P, C>, P : Check.Params<C>>
            ValidationRule<C, V, P>.unaryPlus(): ValidationBlockTermination =
        internalsGate.validationOperation {
            scope.validate(subject, this@unaryPlus).status
        }

    /**
     *
     */
    suspend operator fun <T> T.invoke(
        validationBlock: ValidationBlock<T>
    ): ValidationBlockTermination =
        internalsGate.validationOperation {
            scope.validate {
                scopeOperation {
                    toValidationOf(this@invoke).validationBlock().getOrThrow()
                }
            }.status
        }

    /**
     * Validate given [value][Named] against [validation logic][validationBlock] into
     * a new, [named][Named.name] scope.
     */
    suspend infix fun <T> Named<T>.require(
        validationBlock: ValidationBlock<T>
    ): ValidationBlockTermination =
        internalsGate.validationOperation {
            scope.validate(name) {
                scopeOperation {
                    toValidationOf(value).validationBlock().getOrThrow()
                }
            }.status
        }

    /**
     * "Overloaded" version of [Named.require] function that takes property as
     * a receiver. A new scope's name and value to validate is taken from the property
     * specified ([KProperty0.name] and [KProperty0.get] respectively).
     */
    suspend infix fun <T> KProperty0<T>.require(
        validationBlock: ValidationBlock<T>
    ): ValidationBlockTermination =
        toNamed().require(validationBlock)

    /**
     * DSL version of [runWhenValid] making [current context][ValidationOf] available
     * into passed [validation block][validationBlock].
     */
    suspend fun ValidationResult.whenValid(
        validationBlock: ValidationBlock<V>
    ): ValidationBlockTermination =
        internalsGate.validationOperation {
            runWhenValid { validationBlock() }.getOrThrow()
        }

    /**
     * DSL version of [runWhenFailureOfType] making [current context][ValidationOf]
     * available into passed [validation block][validationBlock].
     */
    suspend inline fun <reified E : Throwable> ValidationResult.recoverFrom(
        crossinline validationBlock: ValidationBlock1<V, E>,
    ): ValidationBlockTermination =
        (this@ValidationOf as Internals).validationOperation {
            runWhenFailureOfType<E> { validationBlock(it) }.getOrThrow()
        }

    sealed interface Internals {
        val scope: ValidationScope
    }
}

private val ValidationOf<*>.internalsGate: ValidationOf.Internals
    get() = this as ValidationOf.Internals

private class ValidationOfInternal<V>(
    subject: V,
    override val scope: ValidationScope
) : ValidationOf<V>(subject), ValidationOf.Internals

/**
 * Utility function for defining functions complaint to the [validation DSL]
 * [ValidationOf], i.e. that can serve as [terminators of a validation blocks]
 * [ValidationBlockTermination].
 */
suspend fun ValidationOf.Internals.validationOperation(
    block: suspend ValidationOf.Internals.() -> ValidationStatus
): ValidationBlockTermination =
    ValidationBlockTerminationInternal(
        validateCatching { block() }
    )

private fun <T> ValidationScope.toValidationOf(value: T): ValidationOf<T> =
    ValidationOfInternal(value, this)

/**
 * Validates each element of given iterable (being a [subject][ValidationOf.subject]
 * of a current context) against provided [validation rules][validationBlock].
 * Each element is validated into new scope indexed according to its position in the
 * iterable. Index of currently validated element is passed to the validation block
 * as a lambda parameter.
 */
suspend fun <T : Iterable<EL>, EL> ValidationOf<T>.eachElement(
    validationBlock: ValidationBlock1<EL, Int>,
): ValidationBlockTermination =
    internalsGate.validationOperation {
        subject.mapIndexed { idx, value ->
            scope.validate(idx.asIndex()) {
                scopeOperation {
                    toValidationOf(value).validationBlock(idx).getOrThrow()
                }
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
): ValidationBlockTermination =
    internalsGate.validationOperation {
        subject.map { entry ->
            val key = entry.key
            val value = entry.value
            scope.validate(displayingKeysAs(key).asKey()) {
                scopeOperation {
                    val valueValidationStatus = validate(!"value") {
                        scopeOperation {
                            toValidationOf(value).valueValidation(key).getOrThrow()
                        }
                    }.status
                    val keyValidationStatus = keyValidation?.let {
                        validate(!"key") {
                            scopeOperation {
                                toValidationOf(key).keyValidation(value).getOrThrow()
                            }
                        }
                    }?.status ?: ValidationStatus.Valid
                    (valueValidationStatus + keyValidationStatus)
                }
            }.status
        }.fold()
    }

typealias ValidationBlock<T> =
        suspend ValidationOf<T>.() -> ValidationBlockTermination

typealias ValidationBlock1<T, T2> =
        suspend ValidationOf<T>.(T2) -> ValidationBlockTermination

sealed interface ValidationBlockTermination : ValidationResult

private class ValidationBlockTerminationInternal(result: ValidationResult) :
    ValidationBlockTermination, ValidationResult by result
