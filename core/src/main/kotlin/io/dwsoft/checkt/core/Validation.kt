package io.dwsoft.checkt.core

import kotlin.reflect.KProperty0

/*
 * TODO:
 *  - correct docks after context removal and receivers refactor
 *  - tests and refactoring collection/map validation in regards of Result
 *  - tests of conditional rule processing
 *  - make Validation implement ValidationSpec ???
 */

///**
// * Validates given value against [spec][ValidationSpec] created out of passed
// * [block][ValidationBlock].
// */
//suspend fun <T> T.validate(
//    namedAs: NonBlankString?,
//    validationBlock: ValidationBlock<T>
//): Result<ValidationResult> =
//    runCatching {
//        val scope = namedAs?.let { ValidationScope(it) } ?: ValidationScope()
//        Validation(this, scope).apply { validationBlock().getOrThrow() }
//        scope.result
//    }

// Todo: add dsl maker???
/**
 * Validation logic builder/DSL.
 *
 * It creates validation context composed of validated value, i.e. [subject] together
 * with a [named scope][ValidationScope] within which defined rules are executed.
 *
 * Each validation operation defined by the [validation DSL][Validation] is run with
 * [exception catching in place][runCatching] and should respond with a
 * [status][ValidationOperationStatus] that represent either exceptional completion
 * or successful one resulting with an actual [validation result][ValidationResult].
 */
class Validation<V>(val subject: V, private val scope: ValidationScope) {
    /**
     * Validate given [value][Named] against [validation logic][validationBlock] into
     * a new, [named][Named.name] scope.
     */
    suspend infix fun <T> Named<T>.require(validationBlock: ValidationBlock<T>) =
        runEnclosed(value, ValidationPath.Segment.Name(name), validationBlock)

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
    infix fun <C : Check<T, P, C>, T : Any?, P : Check.Params<C>> T.require(
        toBeValidAgainst: ValidationRule<C, T, P>,
    ) = checkValueAgainstRule(this, toBeValidAgainst)

    /**
     * Tests the [currently validated value][subject] against given [validation rule][this].
     */
    operator fun <C : Check<V, P, C>, P : Check.Params<C>> ValidationRule<C, V, P>.unaryPlus() =
        subject.require(toBeValidAgainst = this)

    internal suspend fun <T> runEnclosed(
        value: T,
        namedAs: ValidationPath.Segment,
        validationBlock: ValidationBlock<T>,
    ): ValidationOperationStatus =
        runCatching {
            scope.enclose(namedAs).also { enclosedScope ->
                with (Validation(value, enclosedScope)) {
                    validationBlock().getOrThrow()
                }
            }.result
        }

    private fun <C : Check<T, P, C>, T : Any?, P : Check.Params<C>> checkValueAgainstRule(
        value: T,
        rule: ValidationRule<C, T, P>,
    ): ValidationOperationStatus =
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
): ValidationOperationStatus =
    subject.mapIndexed { idx, value ->
        runEnclosed(value, ValidationPath.Segment.Index(idx)) {
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
): ValidationOperationStatus =
    subject.map { entry ->
        val key = entry.key
        val indexSegment = ValidationPath.Segment.Index(displayingKeysAs(key))
        runEnclosed(entry, indexSegment) newScope@{
            val value = entry.value
            val keyValidationResult = keyValidation?.let {
                this@newScope.runEnclosed(
                    key,
                    ValidationPath.Segment.Name(!"key")
                ) keyScope@{ this@keyScope.keyValidation(value) }
            }?.getOrThrow()
            val valueValidationResult = runEnclosed(
                value,
                ValidationPath.Segment.Name(!"value")
            ) valueScope@{ this@valueScope.valueValidation(key) }
                .getOrThrow()
            (keyValidationResult?.let { it + valueValidationResult } ?: valueValidationResult)
                .asValidationBlockResult()
        }
    }.fold()

class Named<T>(val value: T, val name: NonBlankString)

fun <T> T.namedAs(name: NonBlankString): Named<T> = Named(this, name)

fun <T> KProperty0<T>.toNamed(): Named<T> = Named(get(), !name)

typealias ValidationBlock<V> = suspend Validation<V>.() -> ValidationOperationStatus

typealias ValidationBlock1<V, P1> = suspend Validation<V>.(P1) -> ValidationOperationStatus

typealias ValidationOperationStatus = Result<ValidationResult>

private fun Collection<ValidationOperationStatus>.fold(): ValidationOperationStatus =
    map {
        it.fold(
            onFailure = { _ -> return it },
            onSuccess = { validationResult -> return@map validationResult }
        )
    }.reduce(
        ValidationResult::plus
    ).asValidationBlockResult()

private fun ValidationResult.asValidationBlockResult() =
    Result.success(this)
