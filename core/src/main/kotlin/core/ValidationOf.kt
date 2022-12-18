package io.dwsoft.checkt.core

import kotlin.reflect.KProperty0

/*
 * TODO:
 *  - correct docks after scope refactor + tests
 *  - readme
 *  - fail-fast mode
 *  - tests and refactoring collection/map validation in regards of Result
 *  - add dsl maker ???
 */

/**
 * Validation specification represents a reusable validation logic definition,
 * i.e. a function validating value under named scope.
 */
typealias ValidationSpecification<T> = suspend T.(NotBlankString?) -> ValidationStatus

/**
 * Entry point of defining [validation logic][ValidationSpecification] for values
 * of type [T].
 */
fun <T> validation(validationBlock: ValidationBlock<T>): ValidationSpecification<T> =
    { namedAs ->
        validateCatching {
            ValidationOf(this, namedAs)
                // TODO: refactor
                .apply { validationBlock().getOrThrow() }
                .scope.result
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
 * [status][ValidationStatus] that represent either exceptional completion
 * or successful one resulting with an actual [validation result][ValidationResult].
 *
 * [ValidationOf] is also [validation rules "namespace"][ValidationRules], giving
 * access to the variety of [validation rules][ValidationRule] DSL functions.
 */
class ValidationOf<V> internal constructor( // TODO: refactor
    val subject: V,
    internal val scope: ValidationScope,
) : ValidationRules<V> {
    constructor(subject: V, named: NotBlankString? = null): this(
        subject,
        ValidationScope(named?.let { ValidationPath(it) } ?: ValidationPath())
    )

    /**
     * Checks the [currently validated value][subject] against given
     * [validation rule][this].
     */
    suspend operator fun <C : Check<V, P, C>, P : Check.Params<C>>
            ValidationRule<C, V, P>.unaryPlus(): ValidationStatus =
        validateCatching { scope.validate(subject, this) }

    /**
     *
     */
    suspend operator fun <T> T.invoke(
        validationBlock: ValidationBlock<T>
    ): ValidationStatus =
        validateCatching {
            scope.validate {
                toValidationOf(this@invoke).validationBlock().getOrThrow()
            }
        }

    /**
     * Validate given [value][Named] against [validation logic][validationBlock] into
     * a new, [named][Named.name] scope.
     */
    suspend infix fun <T> Named<T>.require(validationBlock: ValidationBlock<T>) =
        validateCatching {
            scope.validate(name) {
                toValidationOf(value).validationBlock().getOrThrow()
            }
        }

    /**
     * "Overloaded" version of [Named.require] function that takes property as
     * a receiver. A new scope's name and value to validate is taken from the property
     * specified ([KProperty0.name] and [KProperty0.get] respectively).
     */
    suspend infix fun <T> KProperty0<T>.require(validationBlock: ValidationBlock<T>) =
        toNamed().require(validationBlock)

    /**
     * DSL version of [runWhenValid] making [current context][ValidationOf] available into
     * passed [validation block][block].
     */
    suspend fun ValidationStatus.whenValid(block: ValidationBlock<V>): ValidationStatus =
        runWhenValid { block() }

    /**
     * DSL version of [runWhenFailureOfType] making [current context][ValidationOf] available
     * into passed [validation block][block].
     */
    suspend inline fun <reified E : Throwable> ValidationStatus.recoverFrom(
        crossinline block: ValidationBlock1<V, E>,
    ): ValidationStatus =
        runWhenFailureOfType<E> { block(it) }
}

private fun <T> ValidationScope.toValidationOf(value: T) = ValidationOf(value, this)

suspend fun validateCatching(block: suspend () -> ValidationResult): ValidationStatus =
    runCatching { block() }

/**
 * Validates each element of given iterable (being a [subject][ValidationOf.subject]
 * of a current context) against provided [validation rules][validationBlock].
 * Each element is validated into new scope indexed according to its position in the
 * iterable. Index of currently validated element is passed to the validation block
 * as a lambda parameter.
 */
suspend fun <T : Iterable<EL>, EL> ValidationOf<T>.eachElement(
    validationBlock: ValidationBlock1<EL, Int>,
): ValidationStatus =
    validateCatching {
        subject.mapIndexed { idx, value ->
            scope.validate(idx.asIndex()) {
                toValidationOf(value).validationBlock(idx).getOrThrow()
            }
        }.fold()
    }

/**
 * Validates each entry of given map (being a [subject][ValidationOf.subject] of a current
 * context) against provided validation rules.
 *
 * Rules can be defined for separately entry's key and value (the former is optional).
 * In both cases "opposite" entry side (value in case of a key, and vice-versa) is passed
 * to the validation lambda as a parameter.
 *
 * Each entry is validated into new scope indexed with entry key transformed by the
 * given [transforming function][displayingKeysAs]. Entry's key and value also have their
 * own scopes created named "key" and "value" respectively.
 */
suspend fun <T : Map<K, V>, K, V> ValidationOf<T>.eachEntry(
    displayingKeysAs: (key: K) -> NotBlankString = { key -> !(key?.toString() ?: "null") },
    keyValidation: ValidationBlock1<K, V>? = null,
    valueValidation: ValidationBlock1<V, K>,
): ValidationStatus =
    validateCatching {
        subject.map { entry ->
            val key = entry.key
            val value = entry.value
            scope.validate(displayingKeysAs(key).asKey()) {
                val valueValidationResult = validate(!"value") {
                    toValidationOf(value).valueValidation(key).getOrThrow()
                }
                val keyValidationResult = keyValidation?.let {
                    validate(!"key") {
                        toValidationOf(key).keyValidation(value).getOrThrow()
                    }
                } ?: ValidationResult.Success
                valueValidationResult + keyValidationResult
            }
        }.fold()
    }

class Named<T>(val value: T, val name: NotBlankString)

fun <T> T.namedAs(name: NotBlankString): Named<T> = Named(this, name)

fun <T> KProperty0<T>.toNamed(): Named<T> = Named(get(), !name)

typealias ValidationBlock<T> = suspend ValidationOf<T>.() -> ValidationStatus

typealias ValidationBlock1<T, T2> = suspend ValidationOf<T>.(T2) -> ValidationStatus
