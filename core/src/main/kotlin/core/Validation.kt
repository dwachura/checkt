package io.dwsoft.checkt.core

import io.dwsoft.checkt.core.ValidationPath.Segment.Index
import io.dwsoft.checkt.core.ValidationPath.Segment.Name
import kotlin.reflect.KProperty0

/*
 * TODO:
 *  - correct docks after context removal and receivers refactor
 *  - readme
 *  - fail-fast mode
 *  - tests and refactoring collection/map validation in regards of Result
 *  - add dsl maker ???
 */

/**
 * Validation specification represents a reusable validation logic definition,
 * i.e. a function validating value under named scope.
 */
typealias ValidationSpecification<T> = suspend T.(NonBlankString?) -> ValidationStatus

/**
 * Entry point of defining [validation logic][ValidationSpecification] for values
 * of type [T].
 */
fun <T> validation(validationBlock: ValidationBlock<T>): ValidationSpecification<T> =
    { namedAs ->
        runCatching {
            val scope = namedAs?.let { ValidationScope(it) } ?: ValidationScope()
            Validation(this, scope).apply { validationBlock().getOrThrow() }
            scope.result
        }
    }

/**
 * Shortcut for immediate invocation of [validation specification][validation].
 */
suspend fun <T> T.validate(
    namedAs: NonBlankString? = null,
    validationBlock: ValidationBlock<T>
) = validation(validationBlock)(this, namedAs)

/**
 * Validation logic builder/DSL.
 *
 * It creates validation context composed of validated value, i.e. [subject] together
 * with a [named scope][ValidationScope] within which defined rules are executed.
 *
 * Each validation operation defined by the [validation DSL][Validation] is run with
 * [exception catching in place][runCatching] and should respond with a
 * [status][ValidationStatus] that represent either exceptional completion
 * or successful one resulting with an actual [validation result][ValidationResult].
 *
 * [Validation] is also [validation rules "namespace"][ValidationRules], giving
 * access to the variety of [validation rules][ValidationRule] DSL functions.
 */
class Validation<V>(val subject: V, private val scope: ValidationScope) : ValidationRules<V> {
    /**
     * Validate given [value][Named] against [validation logic][validationBlock] into
     * a new, [named][Named.name] scope.
     */
    suspend infix fun <T> Named<T>.require(validationBlock: ValidationBlock<T>) =
        runEnclosed(value, Name(name), validationBlock)

    /**
     * "Overloaded" version of [Named.require] function that takes property as
     * a receiver. A new scope's name and value to validate is taken from the property
     * specified ([KProperty0.name] and [KProperty0.get] respectively).
     */
    suspend infix fun <T> KProperty0<T>.require(validationBlock: ValidationBlock<T>) =
        toNamed().require(validationBlock)

    /**
     * Function used to apply given [validation rule][toBeValidAgainst] to the
     * given [value][this]. Any errors are registered under a scope created by the
     * current [context][Validation].
     */
    suspend infix fun <C : Check<T, P, C>, T : Any?, P : Check.Params<C>> T.require(
        toBeValidAgainst: ValidationRule<C, T, P>,
    ) = checkValueAgainstRule(this, toBeValidAgainst)

    /**
     * Tests the [currently validated value][subject] against given [validation rule][this].
     */
    suspend operator fun <C : Check<V, P, C>, P : Check.Params<C>> ValidationRule<C, V, P>.unaryPlus() =
        subject.require(toBeValidAgainst = this)

    /**
     * DSL version of [runWhenValid] making [current context][Validation] available into passed
     * [validation block][block].
     */
    suspend fun ValidationStatus.whenValid(block: ValidationBlock<V>): ValidationStatus =
        runWhenValid { block() }

    /**
     * DSL version of [runWhenFailureOfType] making [current context][Validation] available into
     * passed [validation block][block].
     */
    suspend inline fun <reified E : Throwable> ValidationStatus.recoverFrom(
        crossinline block: ValidationBlock1<V, E>
    ): ValidationStatus =
        runWhenFailureOfType<E> { block(it) }

    internal suspend fun <T> runEnclosed(
        value: T,
        namedAs: ValidationPath.Segment,
        validationBlock: ValidationBlock<T>,
    ): ValidationStatus =
        runCatching {
            scope.enclose(namedAs).also { enclosedScope ->
                with(Validation(value, enclosedScope)) {
                    validationBlock().getOrThrow()
                }
            }.result
        }

    private suspend fun <C : Check<T, P, C>, T : Any?, P : Check.Params<C>> checkValueAgainstRule(
        value: T,
        rule: ValidationRule<C, T, P>,
    ): ValidationStatus =
        runCatching {
            scope.checkValueAgainstRule(value, rule).toValidationResult()
        }
}

/**
 * Validates each element of given iterable (being a [subject][Validation.subject]
 * of a current context) against provided [validation rules][validationBlock].
 * Each element is validated into new scope indexed according to its position in the
 * iterable. Index of currently validated element is passed to the validation block
 * as a lambda parameter.
 */
suspend fun <T : Iterable<EL>, EL> Validation<T>.eachElement(
    validationBlock: ValidationBlock1<EL, Int>,
): ValidationStatus =
    subject.mapIndexed { idx, value ->
        runEnclosed(value, Index(idx)) {
            validationBlock(idx)
        }
    }.fold()

/**
 * Validates each entry of given map (being a [subject][Validation.subject] of a current
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
suspend fun <T : Map<K, V>, K, V> Validation<T>.eachEntry(
    displayingKeysAs: (key: K) -> NonBlankString = { key -> !(key?.toString() ?: "null") },
    keyValidation: ValidationBlock1<K, V>? = null,
    valueValidation: ValidationBlock1<V, K>,
): ValidationStatus =
    subject.map { entry ->
        val key = entry.key
        val indexSegment = Index(displayingKeysAs(key))
        runEnclosed(entry, indexSegment) entry@{
            val value = entry.value
            val valueValidationResult = runEnclosed(value, Name(!"value")) value@{
                this@value.valueValidation(key)
            }.getOrThrow()
            val entryValidationResult = keyValidation?.let {
                val keyValidationResult =
                    this@entry.runEnclosed(key, Name(!"key")) key@{
                        this@key.keyValidation(value)
                    }.getOrThrow()
                keyValidationResult + valueValidationResult
            } ?: valueValidationResult
            entryValidationResult.asValidationStatus()
        }
    }.fold()

class Named<T>(val value: T, val name: NonBlankString)

fun <T> T.namedAs(name: NonBlankString): Named<T> = Named(this, name)

fun <T> KProperty0<T>.toNamed(): Named<T> = Named(get(), !name)

typealias ValidationBlock<T> = suspend Validation<T>.() -> ValidationStatus

typealias ValidationBlock1<T, T2> = suspend Validation<T>.(T2) -> ValidationStatus
